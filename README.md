#Heads-up notifications#
With this app, any device can get the heads-up notifications introduced in Android 5 “Lollipop”, no root required. And the popups actually look like those in Android Lollipop.
But according to Addictivetips (and a lot of the users), this app “has done more than just replicate a feature of Lollipop. Even if you aren’t dying to try Lollipop and some of its new features, this app is worth a try“.

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

## Translating ##
If you speak another language, I'd appreciate if you could help us translate this the app!
[Go to the Crowdin page](https://crowdin.com/project/heads-up)


## Common issues ##
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
A fix for the "Don't disturb" and "Block current app" features is on its way.

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

*PS: Want to try the newest version before everyone else? [Get the BETA here](https://play.google.com/apps/testing/codes.simen.l50notifications).*
