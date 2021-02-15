package launchserver.command.hash;

import launcher.client.ClientProfile;
import launcher.client.ClientProfile.Version;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.serialize.config.TextConfigReader;
import launcher.serialize.config.TextConfigWriter;
import launcher.serialize.config.entry.StringConfigEntry;
import launchserver.LaunchServer;
import launchserver.command.Command;
import launchserver.helpers.UnzipHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

public final class DownloadClientCommand extends Command
{
    public DownloadClientCommand(LaunchServer server)
    {
        super(server);
    }

    @Override
    public String getArgsDescription()
    {
        return "<version> <dir>";
    }

    @Override
    public String getUsageDescription()
    {
        return "Download client dir";
    }

    @Override
    public void invoke(String... args) throws Throwable
    {
        verifyArgs(args, 2);
        String version = args[0];
        String dirName = IOHelper.verifyFileName(args[1]);
        Path clientDir = server.updatesDir.resolve(args[1]);

        // Create client dir
        LogHelper.subInfo("Creating client dir: '%s'", dirName);
        Files.createDirectory(clientDir);

        // Download required client
        LogHelper.subInfo("Downloading client, it may take some time");
        String[] mirrors = server.config.mirrors.stream(StringConfigEntry.class).toArray(String[]::new);
        String clientMask = String.format("clients/%s.zip", version);
        UnzipHelper.downloadZip(mirrors, clientMask, clientDir);

        // Create profile file
        LogHelper.subInfo("Creating profile file: '%s'", dirName);
        ClientProfile client;
        String profilePath;

        // Я бы выпилил это вообще нахуй, но дефолтные конфиги нужны! (Товарищи, партия требует дефолтые конфиги!!!)
        if (Version.compare(version, "1.6.4") <= 0) {
            profilePath = "launchserver/defaults/profile-legacy.cfg";
        } else {
            if (version.contains("-")) {
                String[] versionArgs = version.split("-");
                version = versionArgs[0];
                String modLoader = versionArgs[1];

                if (Arrays.asList("forge", "fabric").contains(modLoader)) {
                    profilePath = String.format("launchserver/defaults/profile-%s.cfg", modLoader);
                } else {
                    profilePath = "launchserver/defaults/profile-default.cfg";
                }
            } else {
                profilePath = "launchserver/defaults/profile-default.cfg";
            }
        }

        try (BufferedReader reader = IOHelper.newReader(IOHelper.getResourceURL(profilePath)))
        {
            client = new ClientProfile(TextConfigReader.read(reader, false));
        }
        client.setTitle(dirName);
        client.setVersion(version);
        client.setAssetIndex(version);
        client.block.getEntry("dir", StringConfigEntry.class).setValue(dirName);
        try (BufferedWriter writer = IOHelper.newWriter(IOHelper.resolveIncremental(server.profilesDir,
                dirName, "cfg")))
        {
            TextConfigWriter.write(client.block, writer, true);
        }

        // Finished
        server.syncProfilesDir();
        server.syncUpdatesDir(Collections.singleton(dirName));
        LogHelper.subInfo("Client successfully downloaded: '%s'", dirName);
        LogHelper.subInfo("DON'T FORGET! Set up the assets directory!");
    }
}
