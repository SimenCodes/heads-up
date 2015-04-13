#Heads-up notifications#
With this app, any device can get the heads-up notifications introduced in the new Android 5.0 “L Developer Preview”, no root required. And the popups actually look like those in Android L.
But according to Addictivetips (and a lot of the users), this app “has done more than just replicate a feature of Android L. Even if you aren’t dying to try Android L and some of its new features, this app is worth a try“.

*Looking for help? Scroll down to "Common Issues"*

## Features ##
- Displays notifications as a floating popup
- Easily control which notifications to display
- Quick actions: mark messages as read in one tap (Android 4.1+, doesn't work with all devices and apps)
- Display the heads-up on lock screen as well
- Battery friendly! (Email me if you notice significant battery consumption after installing)
- Choose how long you want the notifications to display
- Pocket detection / active display (causes a tiny increase in battery usage if you get a ton of notifications)
- Display notifications on the lock screen until dismissed
- Privacy on the lock screen
- Multiple themes
- Choose popup positioning and opacity
- Don't display heads-up while using certain apps
- Display a popup for the currently playing song
- Compact/expandable mode
- Full notification

## So... why is this app on GitHub? ##
As of now, the app has over 100,000 downloads on the Play store, and a 4.5 average rating. And that's really awesome! Honestly, I never thought this thing would make it past 50 downloads, so I'm still recovering after seeing the statistics page on the Play store.

Unfortunately, I don't have time to work on Heads-up, so I decided to open-source it. I'll keep updating the app every now and then, but I just can't keep up with all those great suggestions for new features. And that's where you come in: if you're a developer, designer, translator, or just "some guy", feel free to download the code, make as many changes as you wish, and be sure to send a pull request when you're finished, so everyone else gets to enjoy your improvements.

### Popular feature requests ###
- Answer SMS directly from the popup
- Multiple notifications
- Incoming call handling 
- Reminder after X minutes
- User-submitted themes
- More customization options

I ran a poll on my [website](http://simen.codes/stuff/future-of-heads-up/) a while ago, where over 1,000 users voted.

- Roughly 50% of the users wanted support for multiple notifications. Among those, half wanted them horizontally, while the other half wanted them vertically (this option should probably be implemented in a separate app as it kills the whole point of non-intrusive notifications).
- Then, 25% voted for quick reply to messages. While I specified reply to SMS, many asked for the same feature for other apps as well. AFAIK, most other apps doesn't allow this. If you know how to do this, please share!
- About 10% wanted more advanced theming and customisation. Choosing colors, crating themes, sharing them with friends through the app, etc. Also, some people ask for a separate position on the lock screen.
- The remaining users opted for something else, or keeping the app as it is.

## Translating ##
If you speak another language, I'd appreciate if you could help us translate this the app!
[Go to the Crowdin page](https://crowdin.com/project/heads-up)


## Common issues ##
###*NOTE:* It's exam time now, and I don't have much time to answer your questions.
You can find answers to most questions by using the search box at the top of this page (switch to the desktop version if you don't see it). Thanks for your patience and understanding.
#### If no popups appear: ####
*The universal problemsolver list*
- Did you remember to enable the service by pressing the "Enable/disable" button and then setting Heads-up to ON in the next screen?
- Make sure the service actually is running. Does it show up under System Settings > Apps > Running?
- If not, reboot the phone. Does that help?
- [4.2 and earlier] Do you have any other accessibility services running? Some other services, like Pushbullet, blocks Heads-up from reading notifications.
- Uninstall the app completely, reboot the phone, and install it again. Make sure you are using the [latest version](https://play.google.com/store/apps/details?id=codes.simen.l50notifications). After enabling the service inside the app, reboot the phone.
- Allow more priority levels in the settings.
- Some phones block Heads-up from displaying popups. Check that "draw on top" or "display popups" is enabled in System Settings > Apps > Heads-up.

#### XIAOMI, ZTE and MIUI users: ####
Before you can use this app, you'll probably need to give it some slack when it comes to security.

1. Go to System Settings > All > Apps > Downloaded > Heads-up notifications.
2. Toggle the switch that says 'Show popup windows' (second option).
3. At the bottom, go to 'Manage permissions'.
4. Set 'I trust this app' to on.

*It might also be necessary to reset the app data/settings.*

#### Huawei users: ####
*While I don't have any Huawei devices to test this with, a user said the following works on Huawei Ascend P7:*

Open the app 'phone manager', click settings in the top right corner. Select "dropzone management" and give the app permission.

If this doesn't work, try the instructions for XIAOMI/ZTE/MIUI users.

#### Lollipop users: ####
In Android 5, Google removed the ability to fetch running apps. This makes it impossible to detect custom lock screens, and it also breaks the "Block current app" and "Don't disturb" features. Until Google decides to add this back into Android, it's impossible to implement these feature. They left the following message in the changelog:

> This method was deprecated in API level 21.
> As of LOLLIPOP, this method is no longer available to third party applications: the introduction of document-centric recents means it can leak personal information to the caller. For backwards compatibility, it will still return a small subset of its data: at least the caller's own tasks (though see getAppTasks() for the correct supported way to retrieve that information), and possibly some other tasks such as home that are known to not be sensitive.

#### Device talking? ####
This is a bug in Samsung's firmware. See post on my [website](http://simen.codes/stuff/samsung-phone-talking/). If you don't need Text-To-Speech, you can also go to System Settings > Apps > All, and disable everything with TTS or Text-To-Speech in the name.

#### Uninstalling: ####
If you find that the uninstall button is disabled, go to Settings > Security > Device administrators > Heads-up, and tap Disable.

#### Only one notification ####
Yup, that's right. **Heads-up does only show one notification.** Period. See issue #34.

#### Something else? ####
Something I haven't covered here? I answer all kinds questions in the [issue tracker](https://github.com/SimenCodes/heads-up/issues?q=is%3Aissue).
If your question hasn't been answered there already, feel free to ask!
*Let me know that you've tried the steps here when asking qustions. If you don't, the first thing you'll get is a link to this list.*

## Contact ##
Problems, feature requests or questions? Go to the [Issue tracker](https://github.com/SimenCodes/heads-up/issues?q=is%3Aissue). You can also [email](mailto:sb@simen.codes) me, but I'm slower to answer emails.

*PS: Want to try the newest version before everyone else? [Download the very latest build here](http://app.simen.codes/dev/translation/heads-up/testing.apk).
Please note that it might not work at all, and you have to check for updates manually*
