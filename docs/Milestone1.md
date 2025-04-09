# IT114 Milestone 1

## Instructions
1. Refer to Milestone1 of any of these docs:
  2. [Rock Paper Scissors](https://docs.google.com/document/d/11CxGTMmqVxL6gpzJ-qsc7VAKIJyBBOtOFmewnWdtMEY/view)
  3. [Basic Battleship](https://docs.google.com/document/d/1O1ih9Tqen03Gdr5K44sBCA9llqEsTnloe2OvChw397U/view)
  4. [Hangman / Word guess](https://docs.google.com/document/d/1pZFmyxOwKylrcqxM_rI7YH5x-2YCeOy1BHbI1P0fbVI/view)
  5. [Trivia](https://docs.google.com/document/d/1T0NJtXv6t1ZJz-2x_aH6Ijpzfy47g8zm68XrHNZLtMA/view)
  6. [Go Fish](https://docs.google.com/document/d/1XwpRs2EA0AgB09wBeoHQWcqPADkV4BRfW3SfS6BfC5k/view)
  7. [Pictionary / Drawing](https://docs.google.com/document/d/1vSIv6E7qB_2bjtAhlZ2Shjoxo9YPAdwHa-qdQRRU3lI/view)
8. Ensure you read all instructions and objectives before starting.
9. Ensure you've gone through each lesson related to this Milestone
10. Switch to the Milestone1 branch  
   1. `git checkout Milestone1` (ensure proper starting branch)  
   2. `git pull origin Milestone1` (ensure history is up to date)
11. Copy `Part5` and rename the copy as `Project`
12. Organize the files into their respective packages `Client`, `Common`, `Server`, `Exceptions`
  13. Hint: If it's open, you can refer to the Milestone 2 Prep lesson
14. Fill out the below worksheet
   1. Ensure there's a comment with your UCID, date, and brief summary of the snippet in each screenshot
11. Once finished, click "Submit and Export"  
12. Locally add the generated PDF to a folder of your choosing inside your repository folder and move it to Github  
   1. `git add .`  
   2. `git commit -m "adding PDF"`  
   3. `git push origin Milestone1`  
   4. On Github merge the pull request from `Milestone1` to `main`
11. Upload the same PDF to Canvas  
12. Sync Local  
   1. `git checkout main`  
   2. `git pull origin main`

## Feature: Server can be started via command line and listen to connections
- **Points**: 1

### Evidence
- **Points**: 1
- **Type**: combo
- **Images**:
  - **Weight**: 50%
  - **Details**:
	- Show the terminal output of the server started and listening
	- Show the relevant snippet of the code that waits for incoming connections
- **Response**:
  - **Weight**: 50%
  - **Details**:
	- Briefly explain how the server-side waits for and accepts/handles connections

## Feature: Server should be able to allow more than one Client to be connected at once
- **Points**: 1
### Evidence
- **Points**: 1
- **Type**: combo
- **Images**:
  - **Weight**: 50%
  - **Details**:
	- Show the terminal output of the server receiving multiple connections
	- Show at least 3 Clients connected (best to use the split terminal feature)
	- Show the relevant snippets of code that handle logic for multiple connections
- **Response**:
  - **Weight**: 50%
  - **Details**:
	- Briefly explain how the server-side handles multiple connected clients

## Feature: Server will implement the concept of Rooms (with the default being "Lobby")
- **Points**: 2
### Evidence
- **Points**: 1
- **Type**: combo
- **Images**:
  - **Weight**: 50%
  - **Details**:
	- Show the terminal output of rooms being created, joined, and removed (server-side)
	- Show the relevant snippets of code that handle room management (create, join, leave, remove) (server-side)
- **Response**:
  - **Weight**: 50%
  - **Details**:
	- Briefly explain how the server-side handles room creation, joining/leaving, and removal

## Feature: Client can be started via the command line
- **Points**: 1
### Evidence
- **Points**: 1
- **Type**: combo
- **Images**:
  - **Weight**: 50%
  - **Details**:
	- Show the terminal output of the /name and /connect commands for each of 3 clients (best to use the split terminal feature)
	- Output should show evidence of a successful connection
	- Show the relevant snippets of code that handle the processes for /name, /connect, and the confirmation of being fully setup/connected
- **Response**:
  - **Weight**: 50%
  - **Details**:
	- Briefly explain how the /name and /connect commands work and the code flow that leads to a successful connection for the client

## Feature: Client can create/join rooms
- **Points**: 2
### Evidence
- **Points**: 1
- **Type**: combo
- **Images**:
  - **Weight**: 50%
  - **Details**:
	- Show the terminal output of the /createroom and /joinroom
	- Output should show evidence of a successful creation/join in both scenarios
	- Show the relevant snippets of code that handle the client-side processes for room creation and joining
- **Response**:
  - **Weight**: 50%
  - **Details**:
	- Briefly explain how the /createroom and /join room commands work and the related code flow for each

## Feature: Client can send messages
- **Points**: 1
### Evidence
- **Points**: 1
- **Type**: combo
- **Images**:
  - **Weight**: 50%
  - **Details**:
	- Show the terminal output of a few messages from each of 3 clients
	- Include examples of clients grouped into other rooms
	- Show the relevant snippets of code that handle the message process from client to server-side and back
- **Response**:
  - **Weight**: 50%
  - **Details**:
	- Briefly explain how the message code flow works

## Feature: Disconnection
- **Points**: 1
### Evidence
- **Points**: 1
- **Type**: combo
- **Images**:
  - **Weight**: 50%
  - **Details**:
	- Show examples of clients disconnecting (server should still be active)
	- Show examples of server disconnecting (clients should be active but disconnected)
	- Show examples of clients reconnecting when a server is brought back online
	- Examples should include relevant messages of the actions occuring
	- Show the relevant snippets of code that handle the client-side disconnection process
	- Show the relevant snippets of code that handle the server-side termination process
- **Response**:
  - **Weight**: 50%
  - **Details**:
	- Briefly explain how both client and server gracefully handle their disconnect/termination logic
	
## Misc
- **Points**: 1

### Github Details
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

### WakaTime - Activity
- **Points**: 1
- **Type**: image
- **Details**:
  - Visit the WakaTime.com Dashboard
  - Click `Projects` and find your repository
  - Capture the overall time at the top that includes the repository name
  - Capture the individual time at the bottom that includes the file time
  - Note: The duration isn't relevant for the grade and the visual graphs aren't necessary

### Reflection
- **Points**: 1
- **Type**: heading

#### What did you learn? 
- **Points**: 1
- **Type**: text
- **Details**:
  Briefly answer the question (at least a few decent sentences)

#### What was the easiest part of the assignment? 
- **Points**: 1
- **Type**: text
- **Details**:
  Briefly answer the question (at least a few decent sentences)

#### What was the hardest part of the assignment? 
- **Points**: 1
- **Type**: text
- **Details**:
  Briefly answer the question (at least a few decent sentences)