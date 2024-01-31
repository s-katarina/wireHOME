package projectnwt2023.backend.devices.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import projectnwt2023.backend.devices.State;
import projectnwt2023.backend.devices.dto.PayloadDTO;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.devices.service.interfaces.IGateService;
import projectnwt2023.backend.devices.service.interfaces.ILampService;
import projectnwt2023.backend.devices.dto.TelemetryPayloadDTO;
import projectnwt2023.backend.devices.service.interfaces.*;

@Configuration
public class Beans {
    @Autowired
    IDeviceService deviceService;

    @Autowired
    ILampService lampService;

    @Autowired
    IGateService gateService;

    @Autowired
    IAmbientSensorService ambientSensorService;

    @Autowired
    IAirConditionerService airConditionerService;

    @Autowired
    IChargerService chargerService;

    @Autowired
    ISprinklerService sprinklerService;
    @Autowired
    IWashingMachineService washingMachineService;

    @Value("${mosquitto.username}")
    private String username;

    @Value("${mosquitto.password}")
    private String mosquttoPassword;

    public MqttPahoClientFactory mqttPahoClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[]{"tcp://localhost:1883"});
        options.setUserName(username);
        String password = mosquttoPassword;
        options.setPassword(password.toCharArray());
        options.setCleanSession(true);

        factory.setConnectionOptions(options);

        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel(){
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound(){
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("serverIn",
                mqttPahoClientFactory(), "#");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(2);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler(){
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
//                System.out.println(message.getPayload());
//                System.out.println(topic);
                PayloadDTO payloadDTO = getPayload(message, PayloadDTO.class);
                if (topic == null){
                    System.out.println("null je topic");
                } else if (topic.equals("heartbeat")) {
                    deviceService.changeDeviceState((long) payloadDTO.getDeviceId(), State.online);

                } else if(topic.equals("KILLED")) {
                    System.out.println("lost connection " + payloadDTO.getDeviceId());
                    deviceService.changeDeviceState((long) payloadDTO.getDeviceId(), State.offline);
                }
                else if(topic.equals("ON")) {
                    System.out.println("usao u ukljuci");
                    deviceService.changeDeviceOnOff((long) payloadDTO.getDeviceId(), true);
                }
                else if(topic.equals("OFF")) {
                    deviceService.changeDeviceOnOff((long) payloadDTO.getDeviceId(), false);
                }
                else if (topic.contains("lamp")) {
                    lampService.parseRequest(topic, getPayload(message, PayloadDTO.class));
                } else if (topic.contains("gate")) {
                    gateService.parseRequest(topic, message);
                } else if (topic.contains("ambientSensor")) {
                    ambientSensorService.parseRequest(topic, message);
                } else if (topic.contains("airConditioner")) {
                    airConditionerService.parseRequest(topic, message);
                } else if (topic.contains("charger")) {
                    chargerService.parseRequest(topic, message);
                }   else if (topic.contains("sprinkler")) {
                    sprinklerService.parseRequest(topic, message);
                } else if (topic.contains("washingMachine")) {
                    washingMachineService.parseRequest(topic, message);
                }

//                System.out.println(message.getPayload());
            }
        };
    }

    public static <T> T getPayload(Message<?> message, Class<T> dtoClass) {
        Object payload = message.getPayload();
        String jsonPayload = (String) payload;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonPayload, dtoClass);
        } catch (JsonProcessingException e) {
            try {
                return dtoClass.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("Error creating an instance of " + dtoClass.getSimpleName(), ex);
            }
        }
    }

    @Bean
    public MessageChannel mqttOutboundChannel(){
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(){
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("serverOut", mqttPahoClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("#");
        return  messageHandler;
    }
}
