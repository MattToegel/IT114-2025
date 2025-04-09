# IT114 User Input Challenges

## Instructions
1. Ensure you read all instructions and objectives before starting.
2. Create a new branch from `main` called `M3-Homework`  
   1. `git checkout main` (ensure proper starting branch)  
   2. `git pull origin main` (ensure history is up to date)  
   3. `git checkout -b M3-Homework` (create and switch to branch)  
3. Copy the template code from here: [GitHub Repository - M3 Homework](https://github.com/MattToegel/IT114-2025/tree/Module3-Homework/M3)  
   - It includes CommandLineCalculator, SlashCommandHandler, MadLibsGenerator, a BaseClass and a stories folder with 5 stories (used for MadLibsGenerator). Put all into an `M3` folder or similar (adjust `package` reference at the top if you chose a different folder name).  
   - Immediately record to history  
     - `git add .`  
     - `git commit -m "adding M3 HW baseline files"`  
     - `git push origin M3-Homework`  
     - Create a Pull Request from `M3-Homework` to `main` and keep it open  
4. Fill out the below worksheet  
   - Each Problem requires the following as you work  
     - Ensure there's a comment with your UCID, date, and brief summary of how the problem was solved  
     - Update the `ucid` variable  
     - Code solution (add/commit periodically as needed)  
5. Once finished, click "Submit and Export"  
6. Locally add the generated PDF to a folder of your choosing inside your repository folder and move it to Github  
   1. `git add .`  
   2. `git commit -m "adding PDF"`  
   3. `git push origin M3-Homework`  
   4. On Github merge the pull request from `M3-Homework` to `main`  
7. Upload the same PDF to Canvas  
8. Sync Local  
   1. `git checkout main`  
   2. `git pull origin main`

## Challenge 1 - Command Line Calculator (add/sub)
- **Points**: 3

### Edit the `main` method to solve the requirements
- **Points**: 3
- **Type**: combo
- **Details**:
  - Don't adjust the give code unless noted
  - Challenge 1: Accept two numbers and an operator as command-line arguments (+ and -)
  - Challenge 2: Allow integer and floating-point numbers
    - Ensure correct decimal places in output based on input (e.g., 0.1 + 0.2 → 1 decimal place)
  - Display an error for invalid inputs or unsupported operators
  - Add code to solve the problem (add/commit as needed)

- **Images**:
  - **Weight**: 40%
  - **Details**:
    Two screenshots are expected
    1. Snippet of relevant code showing solution (with ucid/date comment)
    2. Full output of executing the program (Capture 5 variations of tests)

- **URLs**:
  - **Weight**: 20%
  - **Details**:
    Direct link to the file in the homework related branch from Github (should end in `.java`)

- **Response**:
  - **Weight**: 40%
  - **Details**:
    Briefly explain `how` the code solves the challenge (note: this isn't the same as `what` the code does)

## Challenge 2 - Slash Command Handler
- **Points**: 3

### Edit the `main` method to solve the requirements
- **Points**: 3
- **Type**: combo
- **Details**:
  - Don't adjust the give code unless noted
  - Challenge 1: Accept user input as slash commands (Commands are case-insensitive)
    - "/greet <name>" → Prints "Hello, <name>!"
    - "/roll <num>d<sides>" → Roll <num> dice with <sides> and returns a single outcome as "Rolled <num>d<sides> and got <result>!"
    - "/echo <message>" → Prints the message back
    - "/quit" → Exits the program
  - Challenge 2: Print an error for unrecognized commands
  - Challenge 3: Print errors for invalid command formats (when applicable)
  - Add code to solve the problem (add/commit as needed)

- **Images**:
  - **Weight**: 40%
  - **Details**:
    Two screenshots are expected
    1. Snippet of relevant code showing solution (with ucid/date comment)
    2. Full output of executing the program (Capture 3 variations of each command except "/quit")

- **URLs**:
  - **Weight**: 20%
  - **Details**:
    Direct link to the file in the homework related branch from Github (should end in `.java`)

- **Response**:
  - **Weight**: 40%
  - **Details**:
    Briefly explain `how` the code solves the challenges (note: this isn't the same as `what` the code does)

## Challenge 3 - Mad Libs Generator
- **Points**: 3

### Edit the `main` method to solve the challenges
- **Points**: 3
- **Type**: combo
- **Details**:
  - Don't adjust the give code unless noted
  - Ensure you have the `stories` folder with the 5 stories
  - Challenge 1: Load a **random** story from the "stories" folder
  - Challenge 2: Extract **each line** into a collection (i.e., ArrayList)
  - Challenge 3: Prompts user for each placeholder (i.e., <adjective>)
    - Any word the user types is acceptable, no need to verify if it matches the placeholder type
    - Any placeholder with underscores should display with spaces instead
  - Challenge 4: Replace placeholders with user input (assign back to original slot in collection)
  - Add code to solve the problem (add/commit as needed)

- **Images**:
  - **Weight**: 40%
  - **Details**:
    Two screenshots are expected
    1. Snippet of relevant code showing solution (with ucid/date comment)
    2. Full output of executing the program (Capture the process for at least 2 stories)

- **URLs**:
  - **Weight**: 20%
  - **Details**:
    Direct link to the file in the homework related branch from Github (should end in `.java`)

- **Response**:
  - **Weight**: 40%
  - **Details**:
    Briefly explain `how` the code solves the challenges (note: this isn't the same as `what` the code does)


## Misc
- **Points**: 1

### Github Details
- **Points**: 1
- **Type**: combo

- **Images**:
  - **Weight**: 60%
  - **Details**:
    From the Commits tab of the Pull Request screenshot the commit history
    Following minimum should be present

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