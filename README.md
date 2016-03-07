# finch-commander
Android app to execute speech recognition command on the open source robot Finch.

# Progress:
0. NOTE: DOCUMENT/TAKE NOTES DURING ENTIRE PROJECT !!!

1. get familiar with the finch                                                                  [DONE]
    - try various commands, make it move, look at the api                                       [DONE]

2. look for Natural Language Processing (NLP) APIs, algorithms                                  [DONE]
    - choose one for project                                                                    [DONE Nuance API]
    * look into google speech recognition API                                                   [DONE - Nuance API]

3. test NLP API from step 2 with various commands                                               [DONE]
    - make sure it recognizes "advance", "forward", "turn", <numbers>, etc.                     [DONE]

4. make basic Android app to establish bluetooth connection and send simple strings             [DONE]

5. establish bluetooth connection with Android app                                              [DONE]
    - make sure server receives strings and can process them                                    [DONE]

6. incorporate voice recognition on Android app                                                 [DONE]
    - send results to server        

7. define basic movement commands to be recognized by the flinch                                [DONE - MOVE, TURN, SAY]
    - connect API with the Finch                                                                [DONE]

8. Connect the server to the Finch robot. Upload server on RPI
    - set up RPI with bluetooth and java server                                             
    - make sure commands are executed by the Finch

9. 3d print rpi case on top of the flinch with usb battery
