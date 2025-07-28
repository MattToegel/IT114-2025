# IT114 Milestone 2 - Drawing

## Instructions
1. Refer to Milestone2 of [Pictionary / Drawing](https://docs.google.com/document/d/1vSIv6E7qB_2bjtAhlZ2Shjoxo9YPAdwHa-qdQRRU3lI/view)
    1. Complete the features
3. Ensure all code snippets include your ucid, date, and a brief description of what the code does
4. Switch to the `Milestone2` branch
    1. `git checkout Milestone2`
    2. `git pull origin Milestone2`
7. Fill out the below worksheet as you test/demo with 3+ clients in the same session
8. Once finished, click "Submit and Export"
9. Locally add the generated PDF to a folder of your choosing inside your repository folder and move it to Github
    1. `git add .`
    2. `git commit -m "adding PDF"
    3. `git push origin Milestone2`
    4. On Github merge the pull request from `Milestone2` to `main`
14. Upload the same PDF to Canvas
15. Sync Local
    1. `git checkout main`
    2. `git pull origin main`

- Complete each section and task sequentially.
- Review the details and validation criteria for each task.
- Ensure subtasks are completed before the parent task.

---

## Payloads
- **Points**: 1

### Show Payload classes and subclasses <!-- UID: 0cBiQnFH -->
- **Points**: 1
- **Type**: combo
- **Details**:
  - Reqs from the document
    - Provided Payload for applicable items that only need client id, message, and type
    - CoordPayload for sending coordinates and color (for now, default the color to something of your choice)
    - (Optional with caveat) DimensionPayload for sending rows/cols or width/height for the drawing board, if this is not done the CoordPayload can be leveraged for this data
    - PointsPayload for syncing points of players
    - Each payload will be presented by debug output (i.e, properly override the toString() method like the lesson examples)

- **Images**:
  - **Weight**: 50%
  - **Details**:
    - Show the code related to your payloads (Payload, CoordPayload, PointsPayload, and any new ones added)
    - Each payload should have an overriden toString() method showing its internal data

- **Response**:
  - **Weight**: 50%
  - **Details**:
    - Briefly explain the purpose of each payload shown in the screenshots and their properties

## Lifecycle events
- **Points**: 4

### GameRoom Client Add/Remove <!-- UID: UuIHY2fu -->
- **Points**: 1
- **Type**: combo

- **Images**:
  - **Weight**: 50%
  - **Details**:
    - Show the `onClientAdded()` code
    - Show the `onClientRemoved()` code

- **Response**:
  - **Weight**: 50%
  - **Details**:
    - Briefly note the actions that happen in `onClientAdded()` (app data should at least be synchronized to the joining user)
    - Briefly note the actions that happen in `onClientRemoved()` (at least should handle logic for an empty session)

### GameRoom Session Start <!-- UID: r88nM6ow -->
- **Points**: 1
- **Type**: combo
- **Details**:
  -  Reqs from document
        - GameRoom determines the board dimensions and sends the info to clients at game start so they can have the same dimensions locally
        - GameRoom loads the word list into memory from a text file for later use
        - First round is triggered

- **Images**:
  - **Weight**: 50%
  - **Details**:
    - Show the snippet of `onSessionStart()`

- **Response**:
  - **Weight**: 50%
  - **Details**:
    - Briefly explain the logic that occurs here (i.e., setting up initial session state for your project) and next lifecycle trigger

### GameRoom Round Start <!-- UID: QaOR3GIU -->
- **Points**: 1
- **Type**: combo
- **Details**:
  - Reqs from Document
    - The correct word is chosen and removed from the in-memory word list
    - The next drawer is chosen (Initially random, then round-robin)
    - The correct word is sent to the drawer only
    - The blanks of the correct word is sent to each guesser
    - GameRoom round timer begins

- **Images**:
  - **Weight**: 50%
  - **Details**:
    - Show the snippet of `onRoundStart()`

- **Response**:
  - **Weight**: 50%
  - **Details**:
    - Briefly explain the logic that occurs here (i.e., setting up the round for your project)

### GameRoom Round End <!-- UID: eNH73cAT -->
- **Points**: 1
- **Type**: combo
- **Details**:
  - Reqs from Document
    - **Condition 1:** Rounds end when all Players have guessed within the time limit
    - **Condition 2:** Rounds end when the Turn timer expires
    - Clear the drawing board (client-side and server-side)
    - Send the in-progress scoreboard to all clients sorted by highest points to lowest
    - Trigger round start logic (if session end condition not met)

- **Images**:
  - **Weight**: 50%
  - **Details**:
    - Show the snippet of `onRoundEnd()`

- **Response**:
  - **Weight**: 50%
  - **Details**:
    - Briefly explain the logic that occurs here (i.e., cleanup, end checks, and next lifecycle events)

### GameRoom Session End <!-- UID: axVWr8Hr -->
- **Points**: 1
- **Type**: combo
- **Details**:
  - Reqs from Document
    - **Condition:** Session ends when X rounds have passed
    - Send the final scoreboard to all clients sorted by highest points to lowest (icnlude a game over message)
    - Reset the player data for each client server-side and client-side (do not disconnect them or move them to the lobby)
    - Clear the drawing board (client-side and server-side)
    - A new ready check will be required to start a new session

- **Images**:
  - **Weight**: 50%
  - **Details**:
    - Show the snippet of `onSessionEnd()`

- **Response**:
  - **Weight**: 50%
  - **Details**:
    - Briefly explain the logic that occurs here (i.e., cleanup/reset, next lifecycle events)

## Gameroom user action and state
- **Points**: 4

### Client and GameRoom board reference <!-- UID: v6iyeB4e -->
- **Points**: 1
- **Type**: combo
- **Details**:
  - Reqs from document
    - Client and GameRoom should have a "board" reference that holds the color of the pixel at each coordinate
      - In this Milestone, it'll just be one color when drawing and a different default color for an area not drawn (i.e., white for blank and black for drawn)
        - Since MS2 is command line likely [ ] and [x] will be used to show colored vs not colored

- **Images**:
  - **Weight**: 50%
  - **Details**:
    - Show the Client-side board reference code
    - Show the Server-side board reference code
    - Show a Client-side example output from the terminal of the board
    - Show a Server-side example output from the terminal of the board
- **Response**:
	- **Weight**: 50%
	- **Details**:
		- Briefly explain how these are synchronized
  

### Draw Logic <!-- UID: 9NRmwJWS -->
- **Points**: 1
- **Type**: combo
- **Details**:
  - Reqs from document
    - Command: `/draw <x>,<y>`
      - Only the drawer can run this (checked Client-side and Server-side)
      - Check against the Client local board state
        - If the coordinate has already been set, print a message stating such; no payload should be sent in this case
        - Otherwise, sends the coordinate to the server-side (the reply from the Server will apply the change to the Client board-state)
      - GameRoom will update its local board state and relay the action to all connected clients if the state changes
        - If the color already exists at this coordinate, send a message to the sender stating so, and don’t send updates to the connected clients

- **Images**:
  - **Weight**: 50%
  - **Details**:
    - Show the code snippets of the following, and clearly caption each screenshot
    - Show the Client processing of this command (process client command)
    - Show the ServerThread processing of this command (process method)
    - Show the GameRoom handling of this command (handle method)
    - Show the sending/syncing of the results of this command to users (send/sync method)
    - Show the ServerThread receiving this data (send method)
    - Show the Client receiving this data (process method)

- **Response**:
  - **Weight**: 50%
  - **Details**:
    - Briefly explain/list in order the whole flow of this command being handled from the client-side to the server-side and back

### Guess Logic <!-- UID: WGaDRoVM -->
- **Points**: 1
- **Type**: combo
- **Details**:
  - Reqs from document
    - Command: `/guess <word>`
      - GameRoom will check against the correct word
        - If it matches, the client will be recorded in a list of correct guessers
          - A message will be relayed that "X guessed correctly"
          - If all players guessed correctly, the round should end early
          - Points will be awarded based on the order the guessers appear in the list where the first position is worth the most and the last position is worth the least (at least 1 point)
              - Points are stored per player (on the User Object) and synced to all clients
        - If it doesn’t match, a message will be relayed that "X guessed Y and it wasn’t correct"

- **Images**:
  - **Weight**: 50%
  - **Details**:
    - Show the code snippets of the following, and clearly caption each screenshot
    - Show the Client processing of this command (process client command)
    - Show the ServerThread processing of this command (process method)
    - Show the GameRoom handling of this command (handle method)
    - Show the sending/syncing of the results of this command to users (send/sync method)
    - Show the ServerThread receiving this data (send method)
    - Show the Client receiving this data (process method)

- **Response**:
  - **Weight**: 50%
  - **Details**:
    - Briefly explain/list in order the whole flow of this command being handled from the client-side to the server-side and back

## Misc
- **Points**: 1

### Github Details <!-- UID: 2ZWJnyed -->
- **Points**: 1
- **Type**: combo

- **Images**:
  - **Weight**: 60%
  - **Details**:
    From the Commits tab of the Pull Request screenshot the commit history

- **URLs**:
  - **Weight**: 40%
  - **Details**:
    Include the link to the Pull Request (should end in `/pull/#`)

### WakaTime - Activity <!-- UID: N1CWjhr7 -->
- **Points**: 1
- **Type**: image
- **Details**:
  - Visit the WakaTime.com Dashboard
  - Click `Projects` and find your repository
  - Capture the overall time at the top that includes the repository name
  - Capture the individual time at the bottom that includes the file time
  - Note: The duration isn't relevant for the grade and the visual graphs aren't necessary

### Reflection <!-- UID: gSKgU766 -->
- **Points**: 1
- **Type**: heading

#### What did you learn? <!-- UID: I63XOHYU -->
- **Points**: 1
- **Type**: text
- **Details**:
  Briefly answer the question (at least a few decent sentences)

#### What was the easiest part of the assignment? <!-- UID: IsAuwB3u -->
- **Points**: 1
- **Type**: text
- **Details**:
  Briefly answer the question (at least a few decent sentences)

#### What was the hardest part of the assignment? <!-- UID: i9fjaP0o -->
- **Points**: 1
- **Type**: text
- **Details**:
  Briefly answer the question (at least a few decent sentences)