# VR30Defs

RomRaider definitions for Nissan and Infiniti VR30DDTT ECUs. Each candidate XML covers one CALID.

[Download the definitions and checksum companion](https://github.com/mrphyslaw/VR30Defs/releases/tag/v0.1.0-rc.1)

## How to use

1. Match your CALID and factory file's full SHA-256 in [factory-roms.tsv](factory-roms.tsv).
2. Extract the [64-bit Windows build of RomRaider 1.1.0](https://github.com/RomRaider/RomRaider/releases/tag/1.1.0-NOV26-2025) and put `vr30defs-checksum.jar` beside the extracted `RomRaider` folder. Close any running RomRaider session, then run this from inside the folder.

```powershell
.\jdk-11.0.14.1+1-jre\bin\java.exe "-Duser.home=$PWD" -cp ".\RomRaider.jar;..\vr30defs-checksum.jar" com.romraider.ECUExec
```

3. In **Definitions → Definition Manager**, add the matching `CALID.xml`. Open a copy of your factory file and check that RomRaider reports **4/4 Checksums are correct**. For editing, select **View → User Level → 3 Advanced** and use **Save As**.

See the [calibration guide](CALIBRATION_GUIDE.md) for what the controls do and their limits.
