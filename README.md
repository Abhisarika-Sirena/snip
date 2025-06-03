[APP  FLOW.docx](https://github.com/user-attachments/files/20563060/APP.FLOW.docx)

APP  FLOW
Application Setup Files:

SnipApplication.kt:
Entry point of the application
Initializes Firebase
Sets up global app configurations
Forces light mode theme

Authentication Flow Files:
MainActivity.kt:
First activity that launches
Checks if user is logged in
Redirects to either LoginActivity or UserListActivity

LoginActivity.kt:
Handles user login
Contains email/password input fields
Validates credentials with Firebase
Provides navigation to SignupActivity

SignupActivity.kt:
Handles new user registration
Collects user details (name, email, password)
Creates user account in Firebase Auth
Creates user profile in Firestore database

Main User Interface Files:
UserListActivity.kt:
Shows list of all users
Main chat screen
Contains bottom navigation
Handles user selection for starting chats

GroupsActivity.kt:
Displays all groups user is part of
Manages group chat navigation
Contains floating action button for creating new groups
CreateGroupActivity.kt:
Interface for creating new groups
Allows selecting multiple users
Handles group creation in Firebase

ChatActivity.kt:
Handles both one-on-one and group chats
Manages message sending/receiving
Real-time message updates using Firebase





Data Model Files:
User.kt (implied from code):
Data class for user information
Contains userId, name, email, timestamp
Group.kt (implied from code):
Data class for group information
Contains groupId, name, members list

Adapter Files:
UserAdapter.kt (in UserListActivity):
Handles user list display
Binds user data to item_user.xml layout
Manages user item clicks
GroupAdapter.kt (implied):
Handles group list display
Similar structure to UserAdapter
ChatAdapter.kt (implied):
Handles chat message display
Manages different message types



Flow of Execution: 
1.App starts → SnipApplication initializes
2.MainActivity launches
3.If not logged in: LoginActivity → (optional) SignupActivity → UserListActivity 
4.If logged in: Directly to UserListActivity
5.From UserListActivity: Click user → ChatActivity (one-on-one chat) Click Groups tab → GroupsActivity From GroupsActivity → CreateGroupActivity or ChatActivity (group chat)
