// ====== LAUNCHER CONFIG ====== //
var config = {
    dir: "TarkCraft", // Launcher directory
    title: "TarkCraft's Launcher", // Window title
    icons: ["favicon.png"], // Window icon paths

    // Settings defaults
    settingsMagic: 0xC0DE5, // Ancient magic, don't touch
    autoEnterDefault: false, // Should autoEnter be enabled by default?
    fullScreenDefault: false, // Should fullScreen be enabled by default?
    ramDefault: 2048 // Default RAM amount (0 for auto)
};

// ====== DON'T TOUCH! ====== //
var dir = IOHelper.HOME_DIR.resolve(config.dir);
if (JVMHelper.OS_TYPE == JVMHelperOS.MUSTDIE) {
    dir = IOHelper.HOME_DIR_WIN.resolve(config.dir);
}
if (!IOHelper.isDir(dir)) {
    java.nio.file.Files.createDirectory(dir);
}
var defaultUpdatesDir = dir.resolve("updates");
if (!IOHelper.isDir(defaultUpdatesDir)) {
    java.nio.file.Files.createDirectory(defaultUpdatesDir);
}
