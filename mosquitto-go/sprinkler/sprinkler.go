package sprinkler

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"strconv"
	"tim10/mqtt/constants"
	"tim10/mqtt/device"
	"tim10/mqtt/helper"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
	cron "github.com/robfig/cron/v3"
)

type Sprinkler struct {
	device.BaseDevice
	IsOn 				bool `json:"on"`
	ScheduleMode      	bool `json:"scheduleMode"`
	client           	mqtt.Client
	Schedule			Schedule
	cron				*cron.Cron
}

type SprinklerMessageDTO struct {
	device.MessageDTO
	Caller 				string `json:"caller"`
}

type SetOnPayload struct {
    NewOn  				bool   `json:"newOn"`
    Caller 				string `json:"caller"`
}

type Schedule struct {
	StartHour 			int 	`json:"startHour"`
	EndHour 			int 	`json:"endHour"`
	Weekdays 			[]int 	`json:"weekdays"`
	Caller				string 	`json:"caller"`
	Off					bool 	`json:"off"`
}

type ScheduleForBackend struct {
	DeviceId			string 	`json:"deviceId"`
	StartHour 			int 	`json:"startHour"`
	EndHour 			int 	`json:"endHour"`
	Weekdays 			[]int 	`json:"weekdays"`
}


func (sprinkler Sprinkler) pubOnOff(client mqtt.Client, onOff bool, caller string) {
	// Publishes influx write query to mqtt topic, which gets processed by Telegraf
	topic := fmt.Sprintf("sprinkler/%d/%s", sprinkler.Id, "on-telemetry")
	val := 1;
	if (!onOff) {
		val = 0
	}
	data := fmt.Sprintf("sprinkler on/off,device-id=%d value=%d,caller=\"%s\"", sprinkler.Id, val, caller)
	token := client.Publish(topic, 0, false, data)
	token.Wait()

	if token.Error() != nil {
		log.Fatal(token.Error())
	}

	fmt.Println("Message from sprinkler on/off published successfully")
}

func (sprinkler Sprinkler) SubToOnSet(client mqtt.Client) {
	// Subscribe to topic to receive ON/OFF commands
	topic := fmt.Sprintf("sprinkler/%d/%s", sprinkler.Id, "on/set")
	token := client.Subscribe(topic, 1, nil)
	token.Wait()
	fmt.Printf("Subscribed to topic: %s", topic)
}

func (sprinkler Sprinkler) SubToSchedule() {
	// Subscribe to topic to receive ON/OFF commands
	topic := fmt.Sprintf("sprinkler/%d/%s", sprinkler.Id, "schedule/set")
	token := sprinkler.client.Subscribe(topic, 1, nil)
	token.Wait()
	fmt.Printf("Subscribed to topic: %s", topic)
}


func (sprinkler Sprinkler) toggleOn(client mqtt.Client, newOn bool, caller string) bool {
	
	// Tell backend command is applied
	// Publish telemetry

	fmt.Println("In TOGGLE sprinkler on/off")
	fmt.Println("oldOn:", sprinkler.IsOn)
	fmt.Println("newOn:", newOn)
	fmt.Println("caller:", caller)
	fmt.Println("currentTime:", time.Now().String())

	currentTime := time.Now()
	var usedFor string = "ON"
	if !newOn {
		usedFor = "OFF"	}
	var msg SprinklerMessageDTO

	if (sprinkler.IsOn && newOn) || (!sprinkler.IsOn && !newOn) {
		usedFor = "Error"
	} 
	
	message := device.MessageDTO{
		DeviceId:  sprinkler.Id,
		UsedFor:   usedFor,
		TimeStamp: currentTime,
	}
	msg = SprinklerMessageDTO{
		MessageDTO: message,
		Caller:     string(caller),
	}
	
	topic := fmt.Sprintf("sprinkler/%d/%s", sprinkler.Id, "on")

	jsonData, err := json.Marshal(msg)
	if err != nil {
		fmt.Println("JSON convert error - sprinkler on/off")
		usedFor = "Error"
	}
	// Respond to backend that command is applied
	token := client.Publish(topic, 0, false, jsonData)
	token.Wait()
	fmt.Println("At END OF TOGGLE sprinkler on/off")
	fmt.Println("msg:", message)
	if (usedFor != "Error") {
		go sprinkler.pubOnOff(client, sprinkler.IsOn, caller)
		return newOn
	} else {
		return sprinkler.IsOn
	}
}

func (sprinkler Sprinkler) operateOnSchedule() {
	weekdays := ""
	for i, weekday := range sprinkler.Schedule.Weekdays {
		if i == len(sprinkler.Schedule.Weekdays) - 1 {
			weekdays += strconv.Itoa(weekday)
			break;
		}
		weekdays += strconv.Itoa(weekday) + ","
	}

	onScheduleString := fmt.Sprintf("0 %d * * %s", sprinkler.Schedule.StartHour, weekdays)
	offScheduleString := fmt.Sprintf("0 %d * * %s", sprinkler.Schedule.EndHour, weekdays)
	
	fmt.Println("on schedule string: ", onScheduleString)
	fmt.Println("off schedule string: ", offScheduleString)

	sprinkler.cron.AddFunc(onScheduleString, func() {sprinkler.toggleOn(sprinkler.client, true, "sprinkler")})
	sprinkler.cron.AddFunc(offScheduleString, func() {sprinkler.toggleOn(sprinkler.client, false, "sprinkler")})

}

func (sprinkler Sprinkler) updateSchedule(schedule Schedule) {
	sprinkler.cron.Stop()
	sprinkler.Schedule = schedule
	sprinkler.cron = cron.New()
	sprinkler.operateOnSchedule()
	sprinkler.cron.Start()

	// TODO save updated schedule
	topic := fmt.Sprintf("sprinkler/%d/%s", sprinkler.Id, "schedule")

	payload := ScheduleForBackend {
		DeviceId: strconv.Itoa(sprinkler.Id),
		StartHour: schedule.StartHour,
		EndHour: schedule.EndHour,
		Weekdays: schedule.Weekdays,
	}
	
	jsonData, err := json.Marshal(payload)
	if err != nil {
		fmt.Println("JSON convert error - sprinkler update schedule")
	}
	// Respond to backend that command is applied
	token := sprinkler.client.Publish(topic, 0, false, jsonData)
	token.Wait()
	fmt.Println("At END OF sprinkler update schedule")
	fmt.Println("payload", payload)
}


func (sprinkler Sprinkler) turnOffSchedule(schedule Schedule) bool {


	currentTime := time.Now()
	message := device.MessageDTO{
		DeviceId:  sprinkler.Id,
		UsedFor:   "OFF",
		TimeStamp: currentTime,
	} 
	var msg = SprinklerMessageDTO {
		MessageDTO: message,
		Caller:     schedule.Caller,
	}
	
	topic := fmt.Sprintf("sprinkler/%d/%s", sprinkler.Id, "schedule/off")

	jsonData, err := json.Marshal(msg)
	if err != nil {
		fmt.Println("JSON convert error - sprinkler on/off")
		return false
	}

	// Respond to backend that command is applied
	token := sprinkler.client.Publish(topic, 0, false, jsonData)
	token.Wait()
	fmt.Println("At END OF sprinkler turn off schedule")
	fmt.Println("msg:", msg)
	return true
}

func getSprinkler(deviceId int) Sprinkler {

	apiUrl := fmt.Sprintf("%s/sprinkler/%d", constants.ApiUrl, deviceId)
	fmt.Println(apiUrl)

	response, err := http.Get(apiUrl)
	if err != nil {
		fmt.Println("Error making GET request:", err)
		return Sprinkler{}
	}
	defer response.Body.Close()

	body, err := io.ReadAll(response.Body)
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return Sprinkler{}
	}

	var sensorData = Sprinkler{}
	err = json.Unmarshal(body, &sensorData)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return Sprinkler{}
	}

	fmt.Println("Response for GET Sprinkler:", string(body))
	return sensorData

}

var sprinkler Sprinkler = getSprinkler(7)


var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {

	fmt.Printf("Received message: %s from topic: %s\n", msg.Payload(), msg.Topic())
	patternOnline := "\\d+"
	patternOn := "sprinkler/\\d+/on/set"
	patternSchedule := fmt.Sprintf("sprinkler/%d/%s", sprinkler.Id, "schedule/set")

	if helper.IsTopicMatch(patternOnline, msg.Topic()) {
		if string(msg.Payload()) == "ON" {
			changed := sprinkler.TurnOn(client, "ON")
			if changed {
				sprinkler.State = true
			}
			fmt.Println(sprinkler.State)
		}
		if string(msg.Payload()) == "OFF" {
			changed := sprinkler.TurnOff(client, "OFF")
			if changed {
				sprinkler.State = true
			}
		}
	}

	// Received command for toggling On/Off
	if helper.IsTopicMatch(patternOn, msg.Topic()) {

		var payload SetOnPayload
    	err := json.Unmarshal(msg.Payload(), &payload)
		if err != nil {
			log.Printf("Error parsing JSON: %v", err)
			return
		}

		newOn := payload.NewOn
		caller := payload.Caller

		sprinkler.IsOn = sprinkler.toggleOn(client, newOn, caller)
	}

	// Received command for setting schedule
	if helper.IsTopicMatch(patternSchedule, msg.Topic()) {

		var payload Schedule
    	err := json.Unmarshal(msg.Payload(), &payload)
		if err != nil {
			log.Printf("Error parsing JSON: %v", err)
			return
		}
		if (payload.Off) {
			if (sprinkler.turnOffSchedule(payload)) {
				sprinkler.ScheduleMode = false
				sprinkler.cron.Stop()
			}
			return
		}
		sprinkler.updateSchedule(payload)
		
	}

}

var connectHandler mqtt.OnConnectHandler = func(client mqtt.Client) {
	fmt.Println("Connected")
}

var connectLostHandler mqtt.ConnectionLostHandler = func(client mqtt.Client, err error) {
	fmt.Printf("Connect lost: %v", err)
}

func RunSprinkler() {

	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("tcp://%s:%d", constants.Broker, constants.Port))
	opts.SetUsername(constants.Username)
	opts.SetPassword(constants.Password)

	opts.SetDefaultPublishHandler(messagePubHandler)
	opts.OnConnect = connectHandler
	opts.OnConnectionLost = connectLostHandler

	myObj := device.MessageDTO{
		DeviceId:  sprinkler.Id,
		UsedFor:   "Kill",
		TimeStamp: time.Now(),
	}
	jsonData, err := json.Marshal(myObj)
	if err != nil {
		log.Fatal(err)
	}
	opts.SetWill("KILLED", string(jsonData), 1, false)

	client := mqtt.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		panic(token.Error())
	}
	sprinkler.client = client
	sprinkler.cron = cron.New()

	sprinkler.Sub(client)
	sprinkler.SubToOnSet(client)
	sprinkler.SubToSchedule()

	go sprinkler.TakesElectisity(client)
	sprinkler.SendHeartBeat(client)
}
