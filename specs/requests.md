##### in:
```js
{
	"id": "42",
	"procedure": "beginSession",
	"argument": {
    	"user": {
        	"name": "ololo"
        },
        "password": "fuck"
    }
}
```

##### out:
```js
{
    "id": "42",
    "procedure": "beginSession",
    "result": {
        "id": 2,
        "opened": 1399149600,
        "closed": 1399153200,
        "types": [
            "Worker"
        ]
    }
}
```

------------------------------------------------------

##### in:
```js
{
	"id": "42",
	"procedure": "endSession",
	"argument": {
    	"id": 30
     }
}
```

##### out:
```js
{
    "id": "42",
    "procedure": "endSession",
    "result": null
}
```

------------------------------------------------------

##### in:
```js
{
	"id": "42",
	"procedure": "getAssignedTasks", 
	"argument": {
    	"session": {
        	"id":2
        },
        "filters": []
    }
}
```

##### out:
```js
{
    "id": "42",
    "procedure": "getAssignedTasks",
    "result": [
        {
            "id": 1,
            "status": "ASSIGNED",
            "currentWorker": {
                "name": "ololo",
                "types": [
                    "Worker"
                ]
            },
            "availabilityTime": [],
            "consumer": {
                "name": "asd",
                "address": {
                    "representation": "asd"
                },
                "cellPhone": "asd",
                "email": "asd"
            },
            "pestType": {
                "id": 1,
                "name": "crap",
                "describtion": "",
                "requiredEquipmentTypes": [
                    {
                        "id": 1,
                        "name": "trowel"
                    }
                ]
            },
            "problemDescription": "asd",
            "taskHistory": [
                {
                    "instant": {
                        "seconds": 1399148979,
                        "nanos": 948000000
                    },
                    "causer": {
                        "name": "myzone",
                        "types": [
                            "Admin"
                        ]
                    },
                    "comment": "fuck"
                
}            ]
        }
    ]
}
```

------------------------------------------------------

##### in:
```js
{
	"id": "42",
	"procedure": "startTask", 
	"argument": {
    	"workerSession": {
        	"id": 30
        },
        "task": {
        	"id": 1
        },
        "comment": "some msg"
    }
}
```

##### out:
```js
{
    "id": "42",
    "procedure": "startTask",
    "result": null
}
```
