# IT114 Java Problems

## Instructions
1. Ensure you read all instructions and objectives before starting.
2. Create a new branch from `main` called `M2-Homework`
  1. `git checkout main` (ensure proper starting branch)
  2. `git pull origin main` (ensure history is up to date)
  3. `git checkout -b M2-Homework` (create and switch to branch) 
3. Copy the template code from here: https://github.com/MattToegel/IT114-2025/tree/Module2-Homework/M2 
    - It includes Problems 1-4 and a BaseClass. Put all into an `M2` folder or similar (adjust `package` reference at the top if you chose a different folder name).
    - Immediately record to history
        - `git add .`
        - `git commit -m "adding M2 HW baseline files"`
        - `git push origin M2-Homework` 
        - Create a Pull Request from `M2-Homework` to `main` and keep it open
4. Fill out the below worksheet
  - Each Problem requires the following as you work
    - Ensure there's a comment with your ucid, date, and brief summary of how the problem was solved
    - Initial outline/plan of how you'll solve it via comments (add/commit after this stage)
    - Code solution (add/commit periodically as needed)
5. Once finished, click "Submit and Export"
6. Locally add the generated PDF to a folder of your choosing inside your repository folder and move it to Github
  1. `git add .`
  2. `git commit -m "adding PDF"
  3. `git push origin M2-Homework`
  4. On Github merge the pull request from `M2-Homework` to `main`
7. Upload the same PDF to Canvas
8. Sync Local
    1. `git checkout main`
    2. `git pull origin main`


## Problem 1 - Odds
- **Points**: 2
### Edit the `printOdds` method to output odd values of the array
- **Points**: 2
- **Type**: Combo
- **Details**: 
    - Only make edits where noted via provided comments
    - Challenge: Print odd values only in a single line separated by commas
    - Step 1: sketch out plan using comments (include ucid and date)
    - Step 2: Add/commit your outline of comments (required for full credit)
    - Step 3: Add code to solve the problem (add/commit as needed)
- **Images**:
  - **Weight**: 40%
  - **Details**:
Two screenshots are expected
    1. Snippet of relevant code showing solution (with ucid/date comment)
    2. Full output of executing the program

- **URLs**:
  - **Weight**: 20%
  - **Details**:
Direct link to the file in the homework related branch from Github (should end in `.java`)

- **Response**:
  - **Weight**: 40%
  - **Details**:
    Briefly explain `how` the code solves the challenge (note: this isn't the same as `what` the code does)

## Problem 2 - Sum
- **Points**: 2
### Edit the `sumValues` method to sum the array values and present them in a format with exactly two decimal places
- **Points**: 2
- **Type**: Combo
- **Details**: 
    - Only make edits where noted via provided comments
    - Challenge 1: Sum all the values of the passed in array and assign to `total`
    - Challenge 2: Have the sum be represented as a number with exactly 2 decimal
    - Example: 0.1 would be shown as 0.10, 1 would be shown as 1.00, etc
    - Step 1: sketch out plan using comments (include ucid and date)
    - Step 2: Add/commit your outline of comments (required for full credit)
    - Step 3: Add code to solve the problem (add/commit as needed)
- **Images**:
  - **Weight**: 40%
  - **Details**:
Two screenshots are expected
    1. Snippet of relevant code showing solution (with ucid/date comment)
    2. Full output of executing the program

- **URLs**:
  - **Weight**: 20%
  - **Details**:
Direct link to the file in the homework related branch from Github (should end in `.java`)

- **Response**:
  - **Weight**: 40%
  - **Details**:
    Briefly explain `how` the code solves the challenges (note: this isn't the same as `what` the code does)

## Problem 3 - Conversion
- **Points**: 2
### Edit the `bePositive` method to make each value positive, convert it back to the orginal data type, and set it to the proper slot in the `output` array
- **Points**: 2
- **Type**: Combo
- **Details**: 
    - Only make edits where noted via provided comments
    - Challenge 1: Make each value positive
    - Challenge 2: Convert the values back to their original data type and assign it to the proper slot of the `output` array
    - Step 1: sketch out plan using comments (include ucid and date)
    - Step 2: Add/commit your outline of comments (required for full credit)
    - Step 3: Add code to solve the problem (add/commit as needed)
- **Images**:
  - **Weight**: 40%
  - **Details**:
Two screenshots are expected
    1. Snippet of relevant code showing solution (with ucid/date comment)
    2. Full output of executing the program

- **URLs**:
  - **Weight**: 20%
  - **Details**:
Direct link to the file in the homework related branch from Github (should end in `.java`)

- **Response**:
  - **Weight**: 40%
  - **Details**:
    Briefly explain `how` the code solves the challenges (note: this isn't the same as `what` the code does)

## Problem 4 - Strings
- **Points**: 2
### Edit the `transformText` method to solve the challenges
- **Points**: 2
- **Type**: Combo
- **Details**: 
    - Only make edits where noted via provided comments
    - Challenge 1: Remove non-alphanumeric characters except spaces
    - Challenge 2: Convert text to Title Case
    - Challenge 3: Trim leading/trailing spaces and remove duplicate spaces
    - Result 1-3: Assign final phrase to `placeholderForModifiedPhrase`
    - Step 1: sketch out plan using comments (include ucid and date)
    - Step 2: Add/commit your outline of comments (required for full credit)
    - Step 3: Add code to solve the problem (add/commit as needed)
- **Images**:
  - **Weight**: 40%
  - **Details**:
Two screenshots are expected
    1. Snippet of relevant code showing solution (with ucid/date comment)
    2. Full output of executing the program

- **URLs**:
  - **Weight**: 20%
  - **Details**:
Direct link to the file in the homework related branch from Github (should end in `.java`)

- **Response**:
  - **Weight**: 40%
  - **Details**:
    Briefly explain `how` the code solves the challenges (note: this isn't the same as `what` the code does)

### Edit the `transformText` method to solve the extra credit challenge (challenge 4)
- **Points**: +0.9
- **Type**: Combo
- **Details**: 
    - Only make edits where noted via provided comments
    - Challenge 4: Extract middle 3 characters (beginning starts at middle of phrase)
    - Assign result to 'placeholderForMiddleCharacters'
    - If not enough characters assign "Not enough characters"
    - Step 1: sketch out plan using comments (include ucid and date)
    - Step 2: Add/commit your outline of comments (required for full credit)
    - Step 3: Add code to solve the problem (add/commit as needed)
- **Images**:
  - **Weight**: 40%
  - **Details**:
Two screenshots are expected
    1. Snippet of relevant code showing solution (with ucid/date comment)
    2. Full output of executing the program

- **Response**:
    - **Weight**: 50%
    - **Details**:
    Briefly explain `how` the code solves the extra credit challenge (note: this isn't the same as `what` the code does)
## Misc
- **Points**: 2
### Github Details
- **Points**: 1
- **Type**: Combo
- **URLs**:
    - **Weight**: 40%
    - **Details**: Include the link to the Pull Request (should end in `/pull/#`)
- **Images**:
    - **Weight**: 60%
    - **Details**: From the Commits tab of the Pull Request screenshot the commit history
    Following minimum should be present
    - Original baseline
    - Commits for the comment outline of the problems
    - Commits for the solution of the problems
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
#### What did you learn?
- **Points**: 1
- **Type**: text
- **Details**: Briefly answer the question (at least a few decent sentences)
#### What was the easiest part of the assignment?
- **Points**: 1
- **Type**: text
- **Details**: Briefly answer the question (at least a few decent sentences)
#### What was the hardest part of the assignment?
- **Points**: 1
- **Type**: text
- **Details**: Briefly answer the question (at least a few decent sentences)