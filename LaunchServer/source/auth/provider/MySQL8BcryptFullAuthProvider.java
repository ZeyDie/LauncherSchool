package launchserver.auth.provider;

import launcher.helper.CommonHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.VerifyHelper;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.IntegerConfigEntry;
import launcher.serialize.config.entry.ListConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;
import launchserver.auth.AuthException;
import launchserver.auth.MySQL8SourceConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//TODO ZeyDie edition
public final class MySQL8BcryptFullAuthProvider extends AuthProvider {
    private final MySQL8SourceConfig mySQLHolder;

    private final int nameColumnId;
    private final int passwordColumnId;

    private final String query;
    private final String[] queryParams;

    MySQL8BcryptFullAuthProvider(BlockConfigEntry block) {
        super(block);

        this.mySQLHolder = new MySQL8SourceConfig("authProviderPool", block);

        this.nameColumnId = block.getEntryValue("nameColumnId", IntegerConfigEntry.class);
        this.passwordColumnId = block.getEntryValue("passwordColumnId", IntegerConfigEntry.class);

        this.query = VerifyHelper.verify(block.getEntryValue("query", StringConfigEntry.class),
                VerifyHelper.NOT_EMPTY, "MySQL query can't be empty");
        this.queryParams = block.getEntry("queryParams", ListConfigEntry.class).
                stream(StringConfigEntry.class).toArray(String[]::new);
    }

    @Override
    public AuthProviderResult auth(String login, String password, String ip) throws SQLException, AuthException {
        try (Connection c = this.mySQLHolder.getConnection(); PreparedStatement s = c.prepareStatement(query)) {
            String[] replaceParams = {"login", login, "password", password, "ip", ip};
            for (int i = 0; i < this.queryParams.length; i++) {
                s.setString(i + 1, CommonHelper.replace(this.queryParams[i], replaceParams));
            }

            // Execute SQL query
            s.setQueryTimeout(MySQL8SourceConfig.TIMEOUT);

            try (ResultSet set = s.executeQuery()) {
                if (set.next()) {
                    final String sqlPassword = set.getString(this.passwordColumnId);
                    final String sqlPasswordSalted = "$2a" + sqlPassword.substring(3);

                    if (BCrypt.checkpw(password, sqlPasswordSalted))
                        return new AuthProviderResult(set.getString(this.nameColumnId), SecurityHelper.randomStringToken());
                }

                return authError("Incorrect username or password");
            }
        }
    }

    @Override
    public void close() {
        this.mySQLHolder.close();
    }
}