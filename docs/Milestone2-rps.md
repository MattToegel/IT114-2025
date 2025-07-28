# IT114 Milestone 2 - RPS

## Instructions
1. Refer to Milestone2 of [Rock Paper Scissors](https://docs.google.com/document/d/11CxGTMmqVxL6gpzJ-qsc7VAKIJyBBOtOFmewnWdtMEY/view)
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

### Show Payload classes and subclasses <!-- UID: aHILXhmJ -->
- **Points**: 1
- **Type**: combo
- **Details**:
  - Reqs from the document
    - Provided Payload for applicable items that only need client id, message, and type
    - PointsPayload for syncing points of players
    - Each payload will be presented by debug output (i.e, properly override the toString() method like the lesson examples)

- **Images**:
  - **Weight**: 50%
  - **Details**:
    - Show the code related to your payloads (Payload, PointsPayload, and any new ones added)
    - Each payload should have an overriden toString() method showing its internal data

- **Response**:
  - **Weight**: 50%
  - **Details**:
    - Briefly explain the purpose of each payload shown in the screenshots and their properties

## Lifecycle events
- **Points**: 4

### GameRoom Client Add/Remove <!-- UID: E338xf4Z -->
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

### GameRoom Session Start <!-- UID: aFl2ch9H -->
- **Points**: 1
- **Type**: combo
- **Details**:
  -  Reqs from document
        - First round is triggered
  - Reset/set initial state

- **Images**:
  - **Weight**: 50%
  - **Details**:
    - Show the snippet of `onSessionStart()`

- **Response**:
  - **Weight**: 50%
  - **Details**:
    - Briefly explain the logic that occurs here (i.e., setting up initial session state for your project) and next lifecycle trigger

### GameRoom Round Start <!-- UID: NXoLDOfI -->
- **Points**: 1
- **Type**: combo
- **Details**:
  - Reqs from Document
    - Initialize remaining Players’ choices to null (not set)
    - Set Phase to "choosing"
    - GameRoom round timer begins

- **Images**:
  - **Weight**: 50%
  - **Details**:
    - Show the snippet of `onRoundStart()`

- **Response**:
  - **Weight**: 50%
  - **Details**:
    - Briefly explain the logic that occurs here (i.e., setting up the round for your project)

### GameRoom Round End <!-- UID: TlcK7Lcq -->
- **Points**: 1
- **Type**: combo
- **Details**:
  - Reqs from Document
    - **Condition 1:** Round ends when round timer expires
    - **Condition 2:**  Round ends when all active Players have made a choice
    - All Players who are not eliminated and haven’t made a choice will be marked as eliminated
    - Process Battles:
      - Round-robin battles of eligible Players (i.e., Player 1 vs Player 2 vs Player 3 vs Player 1)
        - Determine if a Player loses if they lose the “attack” or if they lose the “defend” (since each Player has two battles each round)
          - Give a point to the winning Player
          - Points will be stored on the Player/User object
          - Sync the points value of the Player to all Clients
        - Relay a message stating the Players that competed, their choices, and the result of the battle
        - Losers get marked as eliminated (Eliminated Players stay as spectators but are skipped for choices and for win checks)
      - Count the number of non-eliminated Players
        - If one, this is your winner (onSessionEnd())
        - If zero, it was a tie (onSessionEnd())
        - If more than one, do another round (onRoundStart())

- **Images**:
  - **Weight**: 50%
  - **Details**:
    - Show the snippet of `onRoundEnd()`

- **Response**:
  - **Weight**: 50%
  - **Details**:
    - Briefly explain the logic that occurs here (i.e., cleanup, end checks, and next lifecycle events)

### GameRoom Session End <!-- UID: VM9I5dvA -->
- **Points**: 1
- **Type**: combo
- **Details**:
  - Reqs from Document
    - **Condition 1:** Session ends when one Player remains (they win)
    - **Condition 2:** Session ends when no Players remain (this is a tie)
    - Send the final scoreboard to all clients sorted by highest points to lowest (include a game over message)
    - Reset the player data for each client server-side and client-side (do not disconnect them or move them to the lobby)
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

### Choice Logic <!-- UID: 9TcQmRb8 -->
- **Points**: 1
- **Type**: combo
- **Details**:
  - Reqs from document
    - Command: `/pick <[r,p,s]>` (user picks one)
      - GameRoom will check if it’s a valid option
      - GameRoom will record the choice for the respective Player
      - A message will be relayed saying that "X picked their choice"
      - If all Players have a choice the round ends

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

### Game Cycle Demo <!-- UID: ADCdvC06 -->
- **Points**: 1
- **Type**: image
- **Details**:
  - Show examples from the terminal of a full session demonstrating each command and progress output
  - This includes battle outcomes, scores and scoreboards, etc
  - Ensure at least 3 Clients and the Server are shown
  - Clearly caption screenshots

## Misc
- **Points**: 1

### Github Details <!-- UID: lBocmdwV -->
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

### WakaTime - Activity <!-- UID: mETix9u2 -->
- **Points**: 1
- **Type**: image
- **Details**:
  - Visit the WakaTime.com Dashboard
  - Click `Projects` and find your repository
  - Capture the overall time at the top that includes the repository name
  - Capture the individual time at the bottom that includes the file time
  - Note: The duration isn't relevant for the grade and the visual graphs aren't necessary

### Reflection <!-- UID: xtPVRhUS -->
- **Points**: 1
- **Type**: heading

#### What did you learn? <!-- UID: C0LA0XG9 -->
- **Points**: 1
- **Type**: text
- **Details**:
  Briefly answer the question (at least a few decent sentences)

#### What was the easiest part of the assignment? <!-- UID: a7LEQ77u -->
- **Points**: 1
- **Type**: text
- **Details**:
  Briefly answer the question (at least a few decent sentences)

#### What was the hardest part of the assignment? <!-- UID: atQ0iqVE -->
- **Points**: 1
- **Type**: text
- **Details**:
  Briefly answer the question (at least a few decent sentences)