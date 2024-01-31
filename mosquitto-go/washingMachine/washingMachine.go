package washingMachine

import (
	"encoding/json"
	"fmt"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"io"
	"log"
	"net/http"
	"sort"
	"strconv"
	"strings"
	"tim10/mqtt/constants"
	"tim10/mqtt/device"
	"tim10/mqtt/helper"
	"time"
)

type WashingMachine struct {
	device.BaseDevice
	Regimes []string `json:"regimes"`
	CurrentAction string `json:"currentAction"`
	Client mqtt.Client
}

type WMTask struct {
	Id int32 `json:"id"`
	StartTime string `json:"startTime"`
	Action string `json:"action"`
}

var connectHandler mqtt.OnConnectHandler = func(client mqtt.Client) {
	fmt.Println("Connected")
}

var connectLostHandler mqtt.ConnectionLostHandler = func(client mqtt.Client, err error) {
	fmt.Printf("Connect lost: %v", err)
}

func getWashingMachine(deviceId int) WashingMachine {

	apiUrl := fmt.Sprintf("%s/washingMachine/%d", constants.ApiUrl, deviceId)
	fmt.Println(apiUrl)

	response, err := http.Get(apiUrl)
	if err != nil {
		fmt.Println("Error making GET request:", err)
		return WashingMachine{}
	}
	defer response.Body.Close()

	body, err := io.ReadAll(response.Body)
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return WashingMachine{}
	}

	var sensorData = WashingMachine{}
	err = json.Unmarshal(body, &sensorData)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return WashingMachine{}
	}

	fmt.Println("Response for GET WashingMachine:", string(body))
	fmt.Println(sensorData)
	return sensorData

}

func getWMTasks(deviceId int32) []WMTask {
	apiUrl := fmt.Sprintf("%s/washingMachine/%d/wmtasks", constants.ApiUrl, deviceId)
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

	var wmtasks = []WMTask{}
	err = json.Unmarshal(body, &wmtasks)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return nil
	}

	fmt.Println("Response for GET WashingMachine WMTasks:", string(body))

	sort.Slice(wmtasks, func(i, j int) bool {
		t1, _ := time.Parse(time.RFC3339, wmtasks[i].StartTime)
		t2, _ := time.Parse(time.RFC3339, wmtasks[j].StartTime)
		return t1.After(t2)
	})

	fmt.Println(wmtasks)
	return wmtasks
}

var washingMachine WashingMachine = getWashingMachine(8)
var wmtasks []WMTask = getWMTasks(8)
var onAutomatic = false
var email = ""

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {

	fmt.Printf("Received message: %s from topic: %s\n", msg.Payload(), msg.Topic())
	patternOn := "\\d+"

	if helper.IsTopicMatch(patternOn, msg.Topic()) {
		if string(msg.Payload()) == "ON" {
			changed := washingMachine.TurnOn(client, "ON")
			if changed {
				washingMachine.State = true
			}
		}
		if string(msg.Payload()) == "OFF" {
			changed := washingMachine.TurnOff(client, "OFF")
			if changed {
				washingMachine.State = false
			}
		}
	}

	if helper.IsTopicMatch("washingMachine/\\d+/request", msg.Topic()) {
		payload := string(msg.Payload())
		tokens := strings.Split(payload, ";")
		email = tokens[1]
		tokens = strings.Split(tokens[0], " ")
		action := strings.ToLower(tokens[1])

		supported := false
		for _, regime := range washingMachine.Regimes {
			if strings.Contains(regime, action) {
				supported = true
				break
			}
		}
		fmt.Println(action)

		if strings.Contains(action, "off") {
			supported = true
		}

		if strings.Contains(action, "automatic") {
			wmtasks = getWMTasks(int32(washingMachine.Id))
		}

		if supported {
			washingMachine.CurrentAction = action
			PubAction(action, email, client)
		} else {
			PubAction("Unsupported", "", client)
		}

		onAutomatic = false
	}
}

func PubAction(action string, email string, client mqtt.Client) {
	topic := fmt.Sprintf("washingMachine/%d/response", washingMachine.Id)
	token := client.Publish(topic, 0, false, action + ";" + strconv.Itoa(washingMachine.Id))
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

func PubEvent(action string, email string, client mqtt.Client) {
	topic := fmt.Sprintf("washingMachine/%d/event", washingMachine.Id)
	data := fmt.Sprintf("washingEvent,device-id=%d,email=%s value=\"%s\"", washingMachine.Id, email, action)
	token := client.Publish(topic, 0, false, data)
	token.Wait()
	if token.Error() != nil {
		log.Fatal(token.Error())
	}
	fmt.Printf("Message event published successfully\n")
}

func RunWashingMachine() {

	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("tcp://%s:%d", constants.Broker, constants.Port))
	opts.SetUsername(constants.Username)
	opts.SetPassword(constants.Password)

	opts.SetDefaultPublishHandler(messagePubHandler)
	opts.OnConnect = connectHandler
	opts.OnConnectionLost = connectLostHandler

	myObj := device.MessageDTO{
		DeviceId:  washingMachine.Id,
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
	washingMachine.Client = client

	fmt.Println(washingMachine)
	washingMachine.Sub(client)
	washingMachine.SubToActionRequest(client)
	go washingMachine.TakesElectisity(client)
	go RunSim()
	washingMachine.SendHeartBeat(client)

}

func RunSim() {

	for {

		if washingMachine.CurrentAction != "automatic" && washingMachine.CurrentAction != "off" {
			runAction(washingMachine.CurrentAction, 15)
		}

		if washingMachine.CurrentAction == "automatic" {
			if !onAutomatic {
				onAutomatic = true
			}
			runAutomatic()
		}

		onAutomatic = false

		fmt.Println(washingMachine.CurrentAction)

		time.Sleep(time.Second * 3)
	}

}

func runAutomatic() {
	for onAutomatic {
		task := WMTask{Id: -1}
		curr := time.Now().UTC()

		for _, wmtask := range wmtasks {
			start, _ := time.Parse(time.RFC3339, wmtask.StartTime)

			fmt.Printf("%s - %s\n", start, curr)

			if curr.After(start) && curr.Before(start.Add(time.Second * 4))  {
				fmt.Println("uso")
				task = wmtask
				break
			}
		}
		fmt.Println(task)
		if task.Id != -1 {
			PubAction("automatic#" + task.Action, email, washingMachine.Client)
			washingMachine.CurrentAction = task.Action
			runAction(task.Action, 15)
			if onAutomatic {
				washingMachine.CurrentAction = "automatic"
				PubAction("automatic", email, washingMachine.Client)
			}
		}

		time.Sleep(time.Second * 3)
	}
}

func runAction(action string, seconds int) {
	start := time.Now()
	for strings.Contains(washingMachine.CurrentAction, action) {
		curr := time.Now()

		if curr.Sub(start).Seconds() >= float64(seconds) {
			washingMachine.CurrentAction = "off"
			PubAction("off", email, washingMachine.Client)
			break
		}

		time.Sleep(time.Second * 3)
	}
}

func (washingMachine WashingMachine) SubToActionRequest(client mqtt.Client) {
	topic := fmt.Sprintf("washingMachine/%d/request", washingMachine.Id)
	token := client.Subscribe(topic, 1, nil)
	token.Wait()
	fmt.Printf("Subscribed to topic: %s", topic)
}
