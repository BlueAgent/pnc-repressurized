# PneumaticCraft: Repressurized

This is a port to Minecraft 1.12.2 of MineMaarten's awesome PneumaticCraft mod: https://github.com/MineMaarten/PneumaticCraft.  It is highly functional at this point, although still likely to be fairly buggy and unstable.

* Alpha-quality builds are available from https://minecraft.curseforge.com/projects/pneumaticcraft-repressurized
* Maven artifacts are available from https://modmaven.k-4u.nl/me/desht/pneumaticcraft/pneumaticcraft-repressurized/

To build this mod from source, clone the repository and do:

```
$ ./gradlew setupDecompWorkspace
$ ./gradlew build
```

...and any IDE-specific steps that you need (``genIntellijRuns`` etc.)

I'm welcoming GitHub issues if you find problems but **please**:
1. Check that there isn't already an issue for your problem
1. Be as descriptive as possible, including stacktraces (link to gist/pastebin/etc. please), and full instructions on reproducing the problem.

This code is based on MineMaarten's 1.8.9 code (see the *MC1.8.8* branch in the above-linked repo), which is fairly functional on 1.8 but missing a lot of client-side models & rendering.  At the time of writing (2 Nov 2017), pretty much all of that is re-implemented for 1.12.2!

Have fun!

## New Features in PneumaticCraft: Repressurized

Pretty much all of the functionality from the 1.7.10 PneumaticCraft is now replicated on 1.12.2, along with a few little extras:

* Aphorism tiles can now be edited (right-click with an empty hand).  They can also now have Minecraft markup (colours, bold/italic/underline/strikethrough) by using Alt + 0-9/a-f/l/m/n/o/r, and there's popup keymapping help if you hold down F1.  Also, drama splash is back, without the Drama Splash mod dependency (that mod hasn't been ported to 1.12).  Drama splash can be disabled in client-side config if you don't like it.
* The kerosene lamp can now burn *any* burnable fuel; better fuels last longer (LPG is the best right now).  That can be disabled in config, to have the old behaviour of burning kerosene only.
* A new tool: the Camouflage Applicator.  This can be used to camouflage pressure tubes, elevator bases & callers, charging stations and pneumatic door bases with pretty much any solid block.  Note that the door base & elevator base no longer have slots for items to camouflage, and the old behaviour of sneak-right-clicking a charging station or elevator caller doesn't work anymore.  Camouflaging elevator frames is currently not possible, but should hopefully be re-introduced in a later release.
* Pressure tubes can be disconnected with a wrench, allowing ends to be closed off, and preventing connections where you don't want them.  Note that pressure tubes are not multiparts in this version (MCMP2 just isn't ready for prime time on 1.12 yet, IMHO).
* The One Probe is supported, and the probe can be crafted with the Pneumatic Helmet to integrate it.
* Vortex Cannon is now more effective at breaking plants and leaves.  You can also use the cannon to fling yourself considerable distances (but beware fall damage!)
* Touching a very cold heatsink (< -30C) will give you a slowness debuff.  Extremely cold heatsinks (< -60C) will also cause damage.  Hot heatsinks (> 60C) still hurt, but don't set you on fire until over 100C.
* GUI problem tab now shows a green tick icon (instead of the red "!" icon) when there are no problems with the machine.

### Roadmap

Features & enhancements tracked for beta release: https://github.com/desht/pnc-repressurized/milestone/2

Features & enhancements tracked for post-beta: https://github.com/desht/pnc-repressurized/milestone/3

### Licensing Information

PneumaticCraft: Repressurized is licensed under the GNU GPLv3: https://www.gnu.org/licenses/gpl-3.0.en.html

PneumaticCraft: Repressurized also includes the following free sound resources, which are licensed separately:

* https://freesound.org/people/ThompsonMan/sounds/237245/ (CC BY 3.0)

