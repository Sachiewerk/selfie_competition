Name: Yahya Almardeny.

Student ID: 20072732.

Project Name: WIT Selfie Competition.

Github Repository URL: https://github.com/John-Almardeny/selfie_competition


**Introduction:**

This Selfie Competition App shall add some social value to the students’ life in WIT and make them get to know each other via having some constructive fun and good memories.

**App Functionality:**

Current Overall Structure: 6 Activities, 4 Fragments and 6 Models.

**GUI Design:**

I followed an Agile approach with sprints of 1-week long each. I have 3 users and in every iteration I get their feedback and reviews, so I change the design accordingly.
I concentrated on the colors that are used in the app to make it visually more appealing. Also, I left an option for the user to change the Theme(colors) of the app in the Settings Fragment.
Added to that, I used in my design a Navigation Drawer to allow users to easily navigate to different parts of the application without having to go through a set of activities.
Generally speaking, the main Ux guidelines reference I followed is the "User Interface Guidelines" published on the official Android website.


**GIT Repository:**

I use Github as a VC and I created a local and remote repo for my application at the same time. After each iteration I add what I've done into the stage, commit with an appropriate message and push to the origin repo remotely.
Please refer to the related Gist at: https://gist.github.com/John-Almardeny

**Details** (Please refer to https://github.com/John-Almardeny/selfie_competition/pulse/monthly):

1 author has pushed 12 commits to master and 12 commits to all branches. 
On master, 96 files have changed and there have been 5,608 additions and 125 deletions.

Github Repository URL: https://github.com/John-Almardeny/selfie_competition


**App in a brief details:**

Activities Functionality:

**Splash Screen:** Logo for app, asynchronous checking with database for the conditional log in (firs time sign, kept signed din and not registered.etc).

**Register:** Create a new user account and join the community (Ask for valid email and password(repeated) and send verification link to the user email and inform the user on app, temporarily I commented out the checking for @wit.ie suffix to allow testing for now.

**Sign In:** with email and password, validate and interact.

**Forgot Password:** Send a reset password link to user's email if they are already registered.

**Update Password:** Validate current password and update it.

**First-time Profile Setup:** After the very first login, it asks for basic user information.

**Main:** Main Activity with a Drawer Navigation.

**Fragments:**

**Profile:** It shows editable user's information and changeable profile picture.

**Settings:** Change the App Theme. Change and Update The Password. Close and Delete the User Account. About WIT Seflie Competition App.

**Competition:** TO-DO in CA2 with additional DB Collections and Nodes.

**Gallery:** TO-DO in CA2 with additional DB Collections and Nodes.

**Models:**

**User:** a Model Class to represent User Collection in Database for JSON implementation and manipulation (Read, Update, Delete, Create).

**SharedPreferences Listener:** NOTE THIS IS NOT USED ANYMORE. KEPT FOR ANY FUTURE USAGE - I developed a way to listen to the changes in values by given list of fields in a given shared preferences file then invoke a given method wrapped by Callable once all fields are updated.

**Helper:** Loads of public static method I developed specifically to accelerate, ease the work and compact it, also to avoid loads of duplicates of code among different activities and classes.

**EditTextViewListener:** a workaround developed to listen to the text changes in a given EditText/TextView and calculate the required width to keep the imageView(icon) shown on the side of EditText/TextView Also set the proper EditText/TextView Width onCreate() so it also covers when phone rotates. It accepts LinearLayout and RelativeLayout as parent, if parent is null, that means the parent is the phone screen itself.

**Course:** All courses in WIT 2018 as en um. Please refer to my Gist on https://gist.github.com/John-Almardeny/54a4fcc43f8358ef88c9d2288a8a9cdf

**DoWithData:** A workaround I developed to Execute Function(s) on specific data after fetching them from Database as that happen asynchronously.

**DoWithDataException:** Custom Exception for DoWithData Class.

**Highlights:**

I developed algorithm to set up the approximate destination size of an image after compressing it from Bitmap to JPEG by practically calculating in reverse order the JPEG Depth user by Android. So it can encode different versions of bitmaps into 64Base String that can be saved locally (in SharedPReferences) or on the cloud (as Firebase only allow for up to 10MB size as String value).


**Database:**

-On the Cloud Firebase database contains -currently- one Users collection with many nodes sorted according to the unique user id, under each there is the user model in JOSN format.

-Authentication facility via email and password.

**Rotation Support:**
This app supports phone rotation via having portrait and landscape layouts. The appropriate layout is set according to the phone orientation at runtime by using a method in the Helper class that analyses the caller activity and uses the Reflection to get the required fields from the R class which represent the layouts themselves.

**Approach Adopted for Persistence:**

I used the Shared Preferences to save and retrieve persistent key-value pairs of primitive data type for the user information and the different size of 64Based Encoded Image.
I followed most of the Android Lint suggestions to maintain persistent code style and improve performance.


**References:**

Firebase Documentation at: https://firebase.google.com/docs/guides/

Android Documentation at: https://developer.android.com/guide/index.html


