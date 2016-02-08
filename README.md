# finch-commander
Android app to execute speech recognition command on the open source robot Finch.

# Progress:
1. get familiar with the finch [DONE]
    - try various commands, make it move, look at the api [DONE]

2. look for Natural Language Processing (NLP) APIs, algorithms [DONE]
    - choose one for project [DONE Nuance API]
      * look into google speech recognition API [DONE - Nuance API]

3. test NLP API from step 2 with various commands [DONE]
    - make sure it recognizes "advance", "forward", "turn", <numbers>, etc. [DONE]

6. find client - server framework, work with bluetooth

7. set up framework on RPI.
    - establish bluetooth connection from android app to the server and send and receive simple strings from the voice recognition API

8. incorporate client - server framework with the Finch.
    - make sure commands are executed by the Finch

9. 3d print rpi case on top of the flinch with usb battery
