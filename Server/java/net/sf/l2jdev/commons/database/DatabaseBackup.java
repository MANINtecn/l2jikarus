package net.sf.l2jdev.commons.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.sf.l2jdev.commons.config.DatabaseConfig;

public class DatabaseBackup
{
	public static void performBackup(String description)
	{
		if (DatabaseConfig.BACKUP_DAYS > 0)
		{
			long cut = LocalDateTime.now().minusDays(DatabaseConfig.BACKUP_DAYS).toEpochSecond(ZoneOffset.UTC);
			Path path = Paths.get(DatabaseConfig.BACKUP_PATH);

			try
			{
				Files.list(path).filter(n -> {
					try
					{
						return Files.getLastModifiedTime(n).to(TimeUnit.SECONDS) < cut;
					}
					catch (Exception var4x)
					{
						return false;
					}
				}).forEach(n -> {
					try
					{
						Files.delete(n);
					}
					catch (Exception var2x)
					{
					}
				});
			}
			catch (Exception var7)
			{
			}
		}

		String mysqldumpPath = System.getProperty("os.name").toLowerCase().contains("win") ? DatabaseConfig.MYSQL_BIN_PATH : "";

		try
		{
			String backupFileName = DatabaseConfig.BACKUP_PATH + description + new SimpleDateFormat("_yyyy_MM_dd_HH_mm'.sql'").format(new Date());
			String databaseName = DatabaseConfig.DATABASE_URL.replace("jdbc:mysql://", "").replaceAll(".*\\/|\\?.*", "");
			String[] command = new String[]
			{
				mysqldumpPath + "mysqldump",
				"-u",
				DatabaseConfig.DATABASE_LOGIN,
				DatabaseConfig.DATABASE_PASSWORD.trim().isEmpty() ? "" : "-p" + DatabaseConfig.DATABASE_PASSWORD,
				databaseName,
				"-r",
				backupFileName
			};
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
		}
		catch (Exception var6)
		{
		}
	}
}
