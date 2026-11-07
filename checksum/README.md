# Checksum companion

Build with OpenJDK from this folder. Use the editor JAR from the 64-bit RomRaider build linked in the main README.

```text
mkdir classes
javac --release 8 -cp path/to/RomRaider.jar -d classes src/com/romraider/maps/checksum/ChecksumNissanSH72533Add16Xor32Sum32.java src/com/romraider/maps/checksum/ChecksumCHECKSUMNISSANSH72533ADD16XOR32SUM32.java
jar --create --file vr30defs-checksum.jar -C classes .
```
