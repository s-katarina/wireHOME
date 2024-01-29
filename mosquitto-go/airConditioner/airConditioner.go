package airConditioner

import (
	"encoding/json"
	"fmt"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"io"
	"log"
	"math/rand"
	"net/http"
	"strconv"
	"strings"
	"tim10/mqtt/constants"
	"tim10/mqtt/device"
	"tim10/mqtt/helper"
	"time"
)

type AirConditioner struct {
	device.BaseDevice
	Regimes []string `json:"regimes"`
	Temp int32 `json:"temp"`
	MinTemp int32 `json:"minTemp"`
	MaxTemp int32 `json:"maxTemp"`
	CurrentAction string `json:"currentAction"`
	Client mqtt.Client
}

type ACInterval struct {
	Id int32 `json:"id"`
	StartTime string `json:"startTime"`
	EndTime string `json:"endTime"`
	Action string `json:"action"`
}

var connectHandler mqtt.OnConnectHandler = func(client mqtt.Client) {
	fmt.Println("Connected")
}

var connectLostHandler mqtt.ConnectionLostHandler = func(client mqtt.Client, err error) {
	fmt.Printf("Connect lost: %v", err)
}

func getAirConditioner(deviceId int) AirConditioner {

	apiUrl := fmt.Sprintf("%s/airConditioner/%d", constants.ApiUrl, deviceId)
	fmt.Println(apiUrl)

	response, err := http.Get(apiUrl)
	if err != nil {
		fmt.Println("Error making GET request:", err)
		return AirConditioner{}
	}
	defer response.Body.Close()

	body, err := io.ReadAll(response.Body)
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return AirConditioner{}
	}

	var sensorData = AirConditioner{}
	err = json.Unmarshal(body, &sensorData)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return AirConditioner{}
	}

	fmt.Println("Response for GET AirConditioner:", string(body))
	fmt.Println(sensorData)
	return sensorData

}

func getAcIntervals(deviceId int32) []ACInterval {
	apiUrl := fmt.Sprintf("%s/airConditioner/%d/intervals", constants.ApiUrl, deviceId)
	fmt.Println(apiUrl)

	response, err := http.Get(apiUrl)
	if err != nil {
		fmt.Println("Error making GET request:", err)
		return nil
	}
	defer response.Body.Close()

	body, err := io.ReadAll(response.Body)
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return nil
	}

	var intervals = []ACInterval{}
	err = json.Unmarshal(body, &intervals)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return nil
	}

	fmt.Println("Response for GET AirConditioner Intervals:", string(body))
	fmt.Println(intervals)
	return intervals
}

var airConditioner AirConditioner = getAirConditioner(4)
var intervals []ACInterval = getAcIntervals(4)
var onAutomatic = false
var email = ""

func SetAirConditioner(id int) {
	airConditioner = getAirConditioner(id)
	intervals = getAcIntervals(int32(id))
}

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {

	fmt.Printf("Received message: %s from topic: %s\n", msg.Payload(), msg.Topic())
	patternOn := "\\d+"

	if helper.IsTopicMatch(patternOn, msg.Topic()) {
		if string(msg.Payload()) == "ON" {
			changed := airConditioner.TurnOn(client, "ON")
			if changed {
				airConditioner.State = true
			}
		}
		if string(msg.Payload()) == "OFF" {
			changed := airConditioner.TurnOff(client, "OFF")
			if changed {
				airConditioner.State = false
			}
		}
	}

	if helper.IsTopicMatch("airConditioner/\\d+/request", msg.Topic()) {
		payload := string(msg.Payload())
		tokens := strings.Split(payload, ";")
		email = tokens[1]
		tokens = strings.Split(tokens[0], " ")
		action := strings.ToLower(tokens[1])

		supported := false
		for _, regime := range airConditioner.Regimes {
			if strings.Contains(regime, action) {
				supported = true
				break
			}
		}
		fmt.Println(action)
		if strings.Contains(action, "temp") {
			supported = setTemp(action, client)
		}

		if strings.Contains(action, "off") {
			supported = true
		}

		if strings.Contains(action, "automatic") {
			intervals = getAcIntervals(int32(airConditioner.Id))
		}

		if supported {
			airConditioner.CurrentAction = action
			PubAction(action, email, client)
		} else {
			PubAction("Unsupported", "", client)
		}

		onAutomatic = false
	}
}

func PubAction(action string, email string, client mqtt.Client) {
	topic := fmt.Sprintf("airConditioner/%d/response", airConditioner.Id)
	token := client.Publish(topic, 0, false, action + ";" + strconv.Itoa(airConditioner.Id))
	token.Wait()
	if token.Error() != nil {
		log.Fatal(token.Error())
	}
	fmt.Println("Message response published successfully")

	if action == "Unsupported" {
		return
	}

	PubEvent(action, email, client)
}

func PubNewTemp(client mqtt.Client) {
	topic := fmt.Sprintf("airConditioner/%d/temp", airConditioner.Id)
	token := client.Publish(topic, 0, false, strconv.Itoa(int(airConditioner.Temp)) + ";" + strconv.Itoa(airConditioner.Id))
	token.Wait()
	if token.Error() != nil {
		log.Fatal(token.Error())
	}
	fmt.Printf("Message temp %d published successfully\n", airConditioner.Temp)
}

func PubEvent(action string, email string, client mqtt.Client) {
	topic := fmt.Sprintf("airConditioner/%d/event", airConditioner.Id)
	data := fmt.Sprintf("airEvent,device-id=%d,email=%s value=\"%s\"", airConditioner.Id, email, action)
	token := client.Publish(topic, 0, false, data)
	token.Wait()
	if token.Error() != nil {
		log.Fatal(token.Error())
	}
	fmt.Printf("Message event published successfully\n")
}

func RunAirConditioner() {

	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("tcp://%s:%d", constants.Broker, constants.Port))
	opts.SetUsername(constants.Username)
	opts.SetPassword(constants.Password)

	opts.SetDefaultPublishHandler(messagePubHandler)
	opts.OnConnect = connectHandler
	opts.OnConnectionLost = connectLostHandler

	myObj := device.MessageDTO{
		DeviceId:  airConditioner.Id,
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
		panic(any(token.Error()))
	}
	airConditioner.Client = client

	fmt.Println(airConditioner)
	airConditioner.Sub(client)
	airConditioner.SubToActionRequest(client)
	go airConditioner.TakesElectisity(client)
	go RunSim()
	airConditioner.SendHeartBeat(client)

}

func RunSim() {

	for {

		if airConditioner.CurrentAction == "colling" {
			colling()
		}

		if airConditioner.CurrentAction == "heating" {
			heating()
		}

		if airConditioner.CurrentAction == "ventilation" {
			ventilation()
		}

		if airConditioner.CurrentAction == "automatic" {
			if !onAutomatic {
				go runAutomatic()
			}
			onAutomatic = true
		}

		time.Sleep(time.Second * 3)
	}

}

func runAutomatic() {
	for onAutomatic {
		currentTime := time.Now()
		hour := currentTime.Hour()
		minute := currentTime.Minute()
		second := currentTime.Second()
		curr := int64(hour) * 60 * 60 + int64(minute) * 60 + int64(second)
		fmt.Printf("%d %d %d %d\n", hour, minute, second, curr)

		foundInterval := false

		for _, interval := range intervals {
			startTime := interval.StartTime
			endTime := interval.EndTime
			var iStart int64 = 0
			var iEnd int64 = 0

			tokens := strings.Split(startTime, ":")
			h, _ := strconv.Atoi(tokens[0])
			m, _ := strconv.Atoi(tokens[1])
			s, _ := strconv.Atoi(tokens[2])
			iStart += int64(h) * 60 * 60 + int64(m) * 60 + int64(s)

			tokens = strings.Split(endTime, ":")
			h, _ = strconv.Atoi(tokens[0])
			m, _ = strconv.Atoi(tokens[1])
			s, _ = strconv.Atoi(tokens[2])
			iEnd += int64(h) * 60 * 60 + int64(m) * 60 + int64(s)
			fmt.Printf("%d %d %d\n", iStart, iEnd, curr)
			if curr > iStart && curr < iEnd {
				foundInterval = true
				if airConditioner.CurrentAction != interval.Action {
					airConditioner.CurrentAction = interval.Action
					PubAction(interval.Action, email, airConditioner.Client)

					if strings.Contains(interval.Action, "temp") {
						setTemp(interval.Action, airConditioner.Client)
					}
				}
			}
		}

		if !foundInterval {
			if airConditioner.CurrentAction != "automatic" {
				airConditioner.CurrentAction = "automatic"
				PubAction("automatic", email, airConditioner.Client)
			}
		}

		time.Sleep(time.Second * 3)
	}
}

func ventilation() {
	if airConditioner.CurrentAction != "ventilation" {
		PubAction(airConditioner.CurrentAction, email, airConditioner.Client)
	}
}

func colling() {
	rnd := rand.Intn(11)
	var affect int32 = 0

	if rnd < 4 {
		affect = 1
	}

	newTemp := airConditioner.Temp - 1 * affect
	if newTemp >= airConditioner.MinTemp && newTemp <= airConditioner.MaxTemp {
		airConditioner.Temp = newTemp
		PubNewTemp(airConditioner.Client)
	}
}

func heating() {
	rnd := rand.Intn(11)
	var affect int32 = 0

	if rnd < 4 {
		affect = 1
	}

	newTemp := airConditioner.Temp + 1 * affect
	if newTemp >= airConditioner.MinTemp && newTemp <= airConditioner.MaxTemp {
		airConditioner.Temp = newTemp
		PubNewTemp(airConditioner.Client)
	}
}

func setTemp(action string, client mqtt.Client) bool {
	var tokens = strings.Split(action, "#")
	fmt.Println(tokens)
	tempStr := tokens[1]
	fmt.Println(tempStr)
	temp, _ := strconv.Atoi(tempStr)
	fmt.Println(temp)

	var supported = false

	if int32(temp) >= airConditioner.MinTemp && int32(temp) <= airConditioner.MaxTemp {
		airConditioner.Temp = int32(temp)
		supported = true
		PubNewTemp(client)
	}

	return supported
}

func (airConditioner AirConditioner) SubToActionRequest(client mqtt.Client) {
	topic := fmt.Sprintf("airConditioner/%d/request", airConditioner.Id)
	token := client.Subscribe(topic, 1, nil)
	token.Wait()
	fmt.Printf("Subscribed to topic: %s", topic)
}