package dev.mrpanda.AlevAtar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

public class Bot extends ListenerAdapter {

	public static HashMap<String, String> config; // bot configuration
	
	public static File log = new File(Paths.get(Paths.get("").toAbsolutePath().toString(), "log.txt").toString()); // log file
	public static File blist = new File(Paths.get(Paths.get("").toAbsolutePath().toString(), "blacklist.txt").toString()); // blacklist file
	public static FileWriter logwriter, blistwriter; // file writers
	public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss"); // log date time format
	public static List<String> blacklist = new ArrayList<String>(); // blacklist itself
	public static Scanner blistscan; // blacklist scanner

	public static void main(String[] args) {
		try {
			config = getConfig();
	
			log("System", "Starting bot...");
			
			// shard manager configuration
			DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(config.get("token"))
			.addEventListeners(new Bot())
			.setActivity(Activity.playing("/alev | /kalp"))
			.setAutoReconnect(true);
			
			builder.build(); // build shard manager
		} catch (Exception ex) {
			log("System", "Exception occured: " + ex.getMessage());
			System.exit(1);
		}
	}

	// when the bot is ready and set, notify and get blacklist
	@Override
	public void onReady(@Nonnull ReadyEvent event) {
		log("System", "Bot started.");
		
		// try to scan the blacklist file
		try {
			blistscan = new Scanner(blist); // create scanner
			
			// add blacklisted IDs to blacklist
			while(blistscan.hasNextLine())
				blacklist.add(blistscan.nextLine());
		// if the file does not exist, try to create
		} catch (FileNotFoundException e) {
			if (!blist.exists()) { // if file does not exist
				try {
					blist.createNewFile(); // create
					log("System", "Blacklist file not found. A new file is created.");
				} catch (IOException e1) { // if cannot create, notify
					log("System", "Could not create blacklist file.");
				}
			}
		}
	}

	// when a guild is ready, update its commands
	@Override
	public void onGuildReady(@Nonnull GuildReadyEvent event) {
		ArrayList<CommandData> commands = new ArrayList<CommandData>();
		
		// alev command
		commands.add(Commands.slash("alev", "Avını aleve boğar.")
				.addOption(OptionType.INTEGER, "adet", "Göndermek istediğin alev adedi. (maks. 792)", true)
				.addOption(OptionType.USER, "hedef", "Avın.", true)); 
		
		// kalp command
		commands.add(Commands.slash("kalp", "Avını sevgiye boğar.")
				.addOption(OptionType.INTEGER, "adet", "Göndermek istediğin kalp adedi. (maks. 792)", true)
				.addOption(OptionType.USER, "hedef", "Avın.", true));
		
		// kapat command
		commands.add(Commands.slash("kapat", "[SINIRLI] Botu kapatır."));
		
		// karaliste command
		commands.add(Commands.slash("karaliste", "[SINIRLI] Kötü çocukları kara listeye alır veya uslu olanları kara listeden çıkartır.")
				.addOption(OptionType.USER, "hedef", "Kara listede değilse eklenecek kötü çocuk veya kara listedeyse silinecek uslu çocuk.", true));
		
		event.getGuild().updateCommands().addCommands(commands).queue(); // update commands
	}

	// when the bot joins a new guild, update its commands
	@Override
	public void onGuildJoin(@Nonnull GuildJoinEvent event) {
		ArrayList<CommandData> commands = new ArrayList<CommandData>();
		
		// alev command
		commands.add(Commands.slash("alev", "Avını aleve boğar.")
				.addOption(OptionType.INTEGER, "adet", "Göndermek istediğin alev adedi. (maks. 792)", true)
				.addOption(OptionType.USER, "hedef", "Avın.", true));
		
		// kalp command
		commands.add(Commands.slash("kalp", "Avını sevgiye boğar.")
				.addOption(OptionType.INTEGER, "adet", "Göndermek istediğin kalp adedi. (maks. 792)", true)
				.addOption(OptionType.USER, "hedef", "Avın.", true));
		
		// kapat command
		commands.add(Commands.slash("kapat", "[SINIRLI] Botu kapatır."));
		
		// karaliste command
		commands.add(Commands.slash("karaliste", "[SINIRLI] Kötü çocukları kara listeye alır veya uslu olanları kara listeden çıkartır.")
				.addOption(OptionType.USER, "hedef", "Kara listede değilse eklenecek kötü çocuk veya kara listedeyse silinecek uslu çocuk.", true));
		
		event.getGuild().updateCommands().addCommands(commands).queue(); // update commands
	}
	
	/**
	 * Adds user to blacklist if they do not added yet or removes the user if they are added already.
	 * @param user : user to be added or removed
	 * @return result
	 */
	public static boolean blacklistAddRemove(User user) {
		// if the blacklist file does not exist, try to create
		if (!blist.exists()) {
			try {
				blist.createNewFile();
				log("System", "Blacklist file not found. A new file is created.");
			} catch (IOException e) {
				log("System", "Could not create blacklist file.");
				return false;
			}
		}
		
		// if the user is already added, remove from blacklist
		if (blacklist.contains(user.getId())) {
			blacklist.remove(user.getId());
		// if the user is not in the list, add
		} else {
			blacklist.add(user.getId());
		}
		
		String ids = String.join("\n", blacklist); // blacklisted IDs

		blist.setWritable(true); // set blacklist file as writable
		// try to write to the file
		try {
			blistwriter = new FileWriter(blist);
			blistwriter.write(ids);
			blistwriter.close();
		// if cannot write, revert the changes
		} catch (IOException e) {
			log("System", "An error occured while updating the blacklist file.");
			
			if (blacklist.contains(user.getId())) {
				blacklist.remove(user.getId());
			} else {
				blacklist.add(user.getId());
			}
			
			return false;
		}
		blist.setWritable(false); // set the file as non-writable again
		
		return true;
	}
	
	/**
	 * Prints and saves a log message.
	 * @param source : source of the message
	 * @param message : message itself
	 */
	public static void log(String source, String message) {
		
		// parse the message and print
		String logMsg = "[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "][" + source + "] " + message + "\n";
		System.out.print(logMsg);
		
		// if the log file does not exist, try to create
		if (!log.exists()) {
			try {
				log.createNewFile();
				System.out.print("[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "][System] Log file not found. A new file is created. (This message will not be recorded.)\n");
			} catch (IOException e) {
				System.out.print("[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "][System] Could not create log file and save the previous event. (This message will not be recorded.)\n");
				return;
			}
		}

		log.setWritable(true); // set log file as writable
		// try to write the file
		try {
			logwriter = new FileWriter(log, true);
			logwriter.append(logMsg);
			logwriter.close();
		// if cannot write, notify
		} catch (IOException e) {
			System.out.println("[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "][System] An error occured while saving the previous event to the log file. (This message will not be recorded.)\n");
		}
		log.setWritable(false); // set the file as non-writable again
	}
	
	// when a slash command is used
	@Override
	public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
		// get the command name, user, and the used guild
		String name = event.getName();
		User user = event.getUser();
		Guild guild = event.getGuild();
		
		// if the command is alev
		if (name.equals("alev")) {
			int count = event.getOption("adet").getAsInt(); // get the quantity
			
			// check if the count is valid
			if (count > 792) {
				event.reply("O kadar çok alev gönderemiyorum. Maksimum limit 792.").setEphemeral(true).queue();
				return;
			} else if (count <= 0) {
				event.reply("Lütfen daha yüksek bir sayı gir.").setEphemeral(true).queue();
				return;
			} else if (event.getOption("hedef").getAsUser().isBot()) {
				event.reply("Bota alev atsan ne olacak? Anlamaz ki...").setEphemeral(true).queue();
				return;
			}

			event.deferReply().complete(); // notify Discord
			
			// if the user is blacklisted, notify and abort
			if(blacklist.contains(event.getUser().getId())) {
				log(guild.getName(), "Blocked " + user.getName() + " from sending fires (sender blacklisted).");
				event.getHook().sendMessage(":poop: Her ne yaptıysan kara listedesin. Otur köşende ağla şimdi.").queue();
				return;
			}

			User target = event.getOption("hedef").getAsUser(); // get the target
			
			// if the target is blacklisted, notify and abort
			if(blacklist.contains(target.getId())) {
				log(guild.getName(), "Blocked " + user.getName() + " from sending fires to " + target.getName() + " (target blacklisted).");
				event.getHook().sendMessage(":poop: Alevlemek istediğiniz **"  + target.getName() + "** kara listede. Bırakın köşesinde ağlasın.").queue();
				return;
			}

			int messageCount = count % 198 == 0 ? count / 198 : (count / 198) + 1; // calculate the message count
			
			// open the target's private channel and send the header message
			PrivateChannel targetPM = target.openPrivateChannel().complete();
			targetPM.sendMessage("**" + user.getName() + "** size " + count + " adet :fire: yolladı!").queue();
			
			int ogCount = count; // backup the quantity
			
			// send messages
			for(int i = 0, rem; i < messageCount; i++) {
				String fire = "";

				rem = count < 198 ? count : 198; // remaining emotes
				
				// add emotes to the message
				for(int j = 0; j < rem; j++) {
					fire += ":fire:";
				}
				
				// send message and go to the next message
				targetPM.sendMessage(fire).queue();
				count -= 198;
			}
			
			// acknowledge the user
			log(guild.getName(), user.getName() + " -> " + target.getName() + " (" + ogCount + " fires)");
			event.getHook().sendMessage(":fire: **" + target.getName() + "** kişisi başarıyla " + ogCount + " defa alevlendi. İyi avlamalar.").queue();
			
		// if the command is kalp
		} else if (name.equals("kalp")) {
			int count = event.getOption("adet").getAsInt(); // get the quantity
			
			// check if the count is valid
			if (count > 792) {
				event.reply("O kadar çok kalp gönderemiyorum. Maksimum limit 792.").setEphemeral(true).queue();
				return;
			} else if (count <= 0) {
				event.reply("Lütfen daha yüksek bir sayı gir.").setEphemeral(true).queue();
				return;
			} else if (event.getOption("hedef").getAsUser().isBot()) {
				event.reply("Bota kalp atsan ne olacak? Anlamaz ki...").setEphemeral(true).queue();
				return;
			}

			event.deferReply().complete(); // notify Discord
			
			// if the user is blacklisted, notify and abort
			if(blacklist.contains(event.getUser().getId())) {
				log(guild.getName(), "Blocked " + user.getName() + " from sending hearts (sender blacklisted).");
				event.getHook().sendMessage(":poop: Her ne yaptıysan kara listedesin. Otur köşende ağla şimdi.").queue();
				return;
			}

			User target = event.getOption("hedef").getAsUser(); // get the target
			
			// if the target is blacklisted, notify and abort
			if(blacklist.contains(target.getId())) {
				log(guild.getName(), "Blocked " + user.getName() + " from sending hearts to " + target.getName() + " (target blacklisted).");
				event.getHook().sendMessage(":poop: Simplemek istediğiniz **"  + target.getName() + "** kara listede. Bırakın köşesinde ağlasın.").queue();
				return;
			}

			int messageCount = count % 198 == 0 ? count / 198 : (count / 198) + 1; // calculate the message count
			
			// open the target's private channel and send the header message
			PrivateChannel targetPM = target.openPrivateChannel().complete();
			targetPM.sendMessage("**" + user.getName() + "** size " + count + " adet :heart: yolladı!").queue();
			
			int ogCount = count; // backup the quantity
			
			// send messages
			for(int i = 0, rem; i < messageCount; i++) {
				String heart = "";

				rem = count < 198 ? count : 198; // remaining emotes
				
				// add emotes to the message
				for(int j = 0; j < rem; j++) {
					heart += ":heart:";
				}
				
				// send message and go to the next message
				targetPM.sendMessage(heart).queue();
				count -= 198;
			}
			
			// acknowledge the user
			log(guild.getName(), user.getName() + " -> " + target.getName() + " (" + ogCount + " hearts)");
			event.getHook().sendMessage(":heart: **" + target.getName() + "** kişisi başarıyla " + ogCount + " defa simplendi. İyi avlamalar.").queue();
			
		// if the command is kapat
		} else if (name.equals("kapat")) {
			// if the command user is the bot owner, shutdown
			if(user.getId().equals(config.get("owner_id"))) {
				event.reply("Kapattık kardeşim.").queue();
				event.getJDA().getShardManager().shutdown();
				log("System", "Bot closed.");
			// if the command user is not the bot owner, notify
			} else {
				event.reply("Beni sadece sahibim kapatabilir.").setEphemeral(true).queue();
			}
			
		// if the command is karaliste
		} else if (name.equals("karaliste")) {
			// if the command user is the bot owner, update the blacklist
			if(user.getId().equals(config.get("owner_id"))) {
				event.deferReply().complete(); // notify Discord
				
				User target = event.getOption("hedef").getAsUser(); // get the target
				
				// if the user is successfully added to the blacklist file
				if(blacklistAddRemove(target)) {
					// acknowledge the user
					if (blacklist.contains(target.getId())) {
						event.getHook().sendMessage("**" + target.getName() + "** kara listeye eklendi.").queue();
						log("System", "Added " + target.getName() + " (" + target.getId() + ") to the blacklist.");
					} else {
						event.getHook().sendMessage("**" + target.getName() + "** kara listeden çıkarıldı.").queue();
						log("System", "Removed " + target.getName() + " (" + target.getId() + ") from the blacklist.");
					}
				// if it is not successful, notify
				} else {
					event.getHook().sendMessage("Kara listeyi güncelleme işlemi sırasında bir hata oluştu. Daha sonra tekrar deneyebilir misin?").setEphemeral(true).queue();
				}
			// if the command user is not the bot owner, notify
			} else {
				event.reply("Beni sadece sahibim kapatabilir.").setEphemeral(true).queue();
			}
		}
	}
	
	// when a message is received
	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) return; // if it is from a bot, abort
		
		// if it is from the private channel, reply
		if(event.isFromType(ChannelType.PRIVATE)) {
			event.getChannel().sendMessage("Savcılığa verildiniz.").queue();
		}
	}

	/**
	 * Get the configuration map.
	 * @return config
	 * @throws IOException
	 */
	public static HashMap<String, String> getConfig() throws IOException {
		
		File configFile = new File(Paths.get(Paths.get("").toAbsolutePath().toString(), "config.json").toString()); // config file

		// create the config map
		HashMap<String, String> config = new HashMap<>();
		config.put("token", "");
		config.put("owner_id", "");
		
		ObjectMapper mapper = new ObjectMapper(); // json mapper
		
		// if the file does not exists
		if (!configFile.exists()) {
			log("System", "Cannot find config file.");
			log("System", "Creating a new one...");
			
			// create a new file and write empty json
			configFile.createNewFile();
			FileWriter fw = new FileWriter(configFile);
			fw.write(mapper.writeValueAsString(config));
			fw.close();
			
			log("System", "Created!");
			log("System", "Please enter your credentials into it and run the script again.");
			System.exit(1); // terminate
		}
		
		// read json and map the keys
		String json = Files.readString(Paths.get(configFile.getAbsolutePath()));
		HashMap<String, String> map = mapper.readValue(json, new TypeReference<HashMap<String, String>>() {});
		
		// if the required keys does not exist
		if (!map.keySet().contains("token") || !map.keySet().contains("owner_id")) {
			log("System", "Cannot find required fields in the config.");
			
			// reset the file
			FileWriter fw = new FileWriter(configFile);
			fw.write(mapper.writeValueAsString(config));
			fw.close();

			log("System", "Please enter your credentials into it and run the script again.");
			System.exit(1); // terminate
		}
		
		// set the necessary keys to the config map
		config.replace("token", map.get("token"));
		config.replace("owner_id", map.get("owner_id"));
		
		return config; // return map
	}
}
