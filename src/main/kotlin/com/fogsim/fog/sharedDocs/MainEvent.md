# Main Event

Event is the language of the fog simulator. All actions happen in the simulator are based on events all the entites speak to each other with event. In this section we will explain how to create an event and how to use it.

for example when we want to send a data from sensor to the gateway we need to create an event. in this case we need to create a **SensorEvent**. 

## Event Structure

All events have a structure. this structure is the same for all events.

```kotlin
open class MainEvent {
    var id: String = "0"
    var data: Data = Data(id="", value = "", sensorType = SensorType.TEMPERATURE)
    var senderId: String = "0"
    var receiverId: String = "0"
    var delay: Int = 0
    var date: Long = System.currentTimeMillis()
    var eventType: EventType = EventType.NONE

    override fun toString(): String {
        return "MainEvent(id='$id', data=$data, senderId='$senderId', receiverId='$receiverId', delay=$delay, date=$date)"
    }
    fun toJson(): String {
        return """
            {
                "id": "$id",
                "data": ${data.toJson()},
                "senderId": "$senderId",
                "receiverId": "$receiverId",
                "delay": $delay,
                "date": $date,
                "eventType": "${eventType.name}"
            }
        """.trimIndent()
    }
}
```

| Variable        |                                                                   Usage                                                                   |
| ------------- |:-----------------------------------------------------------------------------------------------------------------------------------------:|
| id      |                                            id of the event. mainly we put the sender id on it.                                            |
| data      |   the data that we want to send to the target entity. all entities when want to send data to other entites use this field to send data.   |
| senderId      |                                           id of the sender or id of the entity that sends event                                           |
| receiverId      |                                        id of the receiver or id of the entity that receives event                                         |
| delay      | delay of the event. this field is for the event scheduler. for example whe we want to wait 1 second before sending event w use this field |
| date      |                                       date of the event.this field shows creation time of the event                                       |
| eventType      |                   type of the event. **we must select it from the sensor type list** for exapmle sound is a sensor type                   |


- Main event is the base class of all events. all events must extend this class.
- Main event is open class. it means we can extend it and add more fields to it.
- Main event has a toString() method. this method is used to print the event in the console.

```kotlin
override fun toString(): String {
        return "MainEvent(id='$id', data=$data, senderId='$senderId', receiverId='$receiverId', delay=$delay, date=$date)"
    }
```

this method prints the event in the console like this:

```kotlin
MainEvent(id='0', data=Data(id='0', value='0', sensorType=TEMPERATURE), senderId='0', receiverId='0', delay=0, date=1622656800000)
```

- Main event has a toJson() method. this method is used to convert the event to json format.

```kotlin
fun toJson(): String {
        return """
            {
                "id": "$id",
                "data": ${data.toJson()},
                "senderId": "$senderId",
                "receiverId": "$receiverId",
                "delay": $delay,
                "date": $date,
                "eventType": "${eventType.name}"
            }
        """.trimIndent()
    }
``` 

this method converts the event to json format like this:

```json
{
    "id": "0",
    "data": {
        "id": "0",
        "value": "0",
        "sensorType": "TEMPERATURE"
    },
    "senderId": "0",
    "receiverId": "0",
    "delay": 0,
    "date": 1622656800000,
    "eventType": "NONE"
}
```