package dev.mrpanda.AlevAtar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import okhttp3.OkHttpClient;

public class Bot extends ListenerAdapter {
	
	public static String DISCORD_TOKEN = ""; // Discord token
	
	public static File log = new File(Paths.get("").toAbsolutePath().toString() + "\\log.txt");
	public static File blist = new File(Paths.get("").toAbsolutePath().toString() + "\\blacklist.txt");
	public static FileWriter logwriter, blistwriter;
	public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss");
	public static List<String> blacklist;
	public static Scanner blistscan;
	public static void main(String[] args) throws LoginException, InterruptedException, IOException {
		@SuppressWarnings("unused")
		JDA jda = JDABuilder.createDefault(DISCORD_TOKEN)
		.enableIntents(GatewayIntent.GUILD_MEMBERS).addEventListeners(new Bot()).setMemberCachePolicy(MemberCachePolicy.ALL)
		.setActivity(Activity.playing("-alev | -kalp")).setAutoReconnect(true).build().awaitReady();
		
		if(!log.exists())
			log.createNewFile();
		
		if(!blist.exists())
			blist.createNewFile();
		
		String started = "[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "]" + " Bot started.\n";
		System.out.print(started);
		logwriter = new FileWriter(log, true);
		logwriter.append(started);
		logwriter.close();
		
		blistscan = new Scanner(blist);
		blacklist = new ArrayList<String>();
		
		while(blistscan.hasNextLine())
			blacklist.add(blistscan.nextLine());
		
		updateBlacklist();
	}
	
	public static void updateBlacklist() throws IOException {
		if(!blist.exists())
			blist.createNewFile();
		
		String ids = "";
		
		for(String id : blacklist) {
			ids += id + "\n";
		}
		
		blist.setWritable(true);
		blistwriter = new FileWriter(blist);
		blistwriter.write(ids);
		blistwriter.close();
		blist.setWritable(false);
	}
	
	public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
		MessageChannel ch = event.getChannel();
		
		if (event.getAuthor().isBot()) return;
		else { ch.sendMessage("Savcýlýða verildiniz.").queue(); return;}
	}
	
	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		if(event.isFromType(ChannelType.PRIVATE)) return;
		
		JDA jda = event.getJDA();
		
		User u = event.getAuthor();
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();
		
		String[] msgRaw = message.getContentDisplay().split(" ");
		String[] msg = Arrays.stream(msgRaw)
                .filter(value ->
                        value != null && value.length() > 0
                )
                .toArray(size -> new String[size]);
		
		if(msg[0].toLowerCase().equals("-alev")) {
			if(msg.length < 3 || message.getMentionedUsers().size() < 1) {
				channel.sendMessage("Komut kullanýmý: -alev [sayý] [hedefler] " + u.getAsMention()).queue();
				return;
			}
			
			int count = 0;
			
			try {
				count = Integer.parseInt(msg[1]);
			} catch(NumberFormatException ex) {
				channel.sendMessage("Komut kullanýmý: -alev [sayý] [hedefler] " + u.getAsMention()).queue();
				return;
			}
			
			if(count <= 0) {
				channel.sendMessage("Lütfen daha yüksek bir sayý gir. " + u.getAsMention()).queue();
				return;
			}
			
			if(count > 792) {
				channel.sendMessage("O kadar çok alev gönderemiyorum. Maksimum limit 792. " + u.getAsMention()).queue();
				return;
			}
			
			if(blacklist.contains(u.getId())) {
				String blockedFire = "[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "][" + event.getGuild().getName() + "] Blocked " + u.getName() + " from sending fires (blacklisted).\n";
				
				System.out.print(blockedFire);
				try {
					if(!log.exists())
						log.createNewFile();
					
					logwriter = new FileWriter(log, true);
					logwriter.append(blockedFire);
					logwriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				channel.sendMessage("Her ne yaptýysan kara listedesin. Otur köþende aðla þimdi. :poop: " + u.getAsMention()).queue();
				return;
			}
			
			String info = "**" + u.getName() + "** size " + count + " adet :fire: yolladý!";
			
			int messageCount;
			if(count % 198 == 0)
				messageCount = count / 198;
			else
				messageCount = (count / 198) + 1;
			
			List<User> targets = message.getMentionedUsers();
			
			for(User target : targets) {
				if(!blacklist.contains(target.getId())) {
					target.openPrivateChannel().complete().sendMessage(info).queue();
					
					int countTemp = count;
					
					for(int i = 0; i < messageCount; i++) {
						String fire = "";
						if(countTemp > 0 && countTemp <= 198) {
							for(int j = 0; j < countTemp; j++) {
								fire += ":fire:";
							}
						} else {
							for(int j = 0; j < 198; j++) {
								fire += ":fire:";
							}
						}
						
						target.openPrivateChannel().complete().sendMessage(fire).queue();
						countTemp -= 198;
					}
	
					String sentFire = "[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "][" + event.getGuild().getName() + "] " + u.getName() + " -> " + target.getName() + " (" + count + " fires)\n";
					
					System.out.print(sentFire);
					try {
						if(!log.exists())
							log.createNewFile();
						
						logwriter = new FileWriter(log, true);
						logwriter.append(sentFire);
						logwriter.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					channel.sendMessage(":fire: **" + target.getName() + "** kiþisi baþarýyla " + count + " defa alevlendi. Ýyi avlamalar. " + u.getAsMention()).queue();
				} else {
					String blockedFire = "[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "][" + event.getGuild().getName() + "] " + u.getName() + " -/-> " + target.getName() + " (blacklisted) (" + count + " fires)\n";
					
					System.out.print(blockedFire);
					try {
						if(!log.exists())
							log.createNewFile();
						
						logwriter = new FileWriter(log, true);
						logwriter.append(blockedFire);
						logwriter.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					channel.sendMessage(":poop: Alevlemek istediðiniz **"  + target.getName() + "** kara listede. Býrakýn köþesinde aðlasýn. " + u.getAsMention()).queue();
				}
			}
		} else if(msg[0].toLowerCase().equals("-kalp")) {
			if(msg.length < 3 || message.getMentionedUsers().size() < 1) {
				channel.sendMessage("Komut kullanýmý: -kalp [sayý] [hedefler] " + u.getAsMention()).queue();
				return;
			}
			
			int count = 0;
			
			try {
				count = Integer.parseInt(msg[1]);
			} catch(NumberFormatException ex) {
				channel.sendMessage("Komut kullanýmý: -kalp [sayý] [hedefler] " + u.getAsMention()).queue();
				return;
			}
			
			if(count <= 0) {
				channel.sendMessage("Lütfen daha yüksek bir sayý gir." + u.getAsMention()).queue();
				return;
			}
			
			if(count > 792) {
				channel.sendMessage("O kadar çok kalp gönderemiyorum. Maksimum limit 792. " + u.getAsMention()).queue();
				return;
			}
			
			if(blacklist.contains(u.getId())) {
				String blockedHeart = "[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "][" + event.getGuild().getName() + "] Blocked " + u.getName() + " from sending hearts (blacklisted).\n";
				
				System.out.print(blockedHeart);
				try {
					if(!log.exists())
						log.createNewFile();
					
					logwriter = new FileWriter(log, true);
					logwriter.append(blockedHeart);
					logwriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				channel.sendMessage("Her ne yaptýysan kara listedesin. Otur köþende aðla þimdi. :poop: " + u.getAsMention()).queue();
				return;
			}
			
			String info = "**" + u.getName() + "** size " + count + " adet :heart: yolladý!";
			
			int messageCount;
			if(count % 198 == 0)
				messageCount = count / 198;
			else
				messageCount = (count / 198) + 1;
			
			List<User> targets = message.getMentionedUsers();
			
			for(User target : targets) {
				if(!blacklist.contains(target.getId())) {
					target.openPrivateChannel().complete().sendMessage(info).queue();
					
					int countTemp = count;
					
					for(int i = 0; i < messageCount; i++) {
						String heart = "";
						if(countTemp > 0 && countTemp <= 198) {
							for(int j = 0; j < countTemp; j++) {
								heart += ":heart:";
							}
						} else {
							for(int j = 0; j < 198; j++) {
								heart += ":heart:";
							}
						}
						
						target.openPrivateChannel().complete().sendMessage(heart).queue();
						countTemp -= 198;
					}
	
					String sentHeart = "[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "][" + event.getGuild().getName() + "] " + u.getName() + " -> " + target.getName() + " (" + count + " hearts)\n";
					
					System.out.print(sentHeart);
					try {
						if(!log.exists())
							log.createNewFile();
						
						logwriter = new FileWriter(log, true);
						logwriter.append(sentHeart);
						logwriter.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					channel.sendMessage(":heart: **" + target.getName() + "** kiþisi baþarýyla " + count + " defa simplendi. Ýyi avlamalar. " + u.getAsMention()).queue();
				} else {
					String blockedHeart = "[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "][" + event.getGuild().getName() + "] " + u.getName() + " -/-> " + target.getName() + " (blacklisted) (" + count + " hearts)\n";
					
					System.out.print(blockedHeart);
					try {
						if(!log.exists())
							log.createNewFile();
						
						logwriter = new FileWriter(log, true);
						logwriter.append(blockedHeart);
						logwriter.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					channel.sendMessage(":poop: Simplemek istediðiniz **"  + target.getName() + "** kara listede. Býrakýn köþesinde aðlasýn. " + u.getAsMention()).queue();
				}
			}
		} else if(msg[0].toLowerCase().equals("-kapat") && u.getId().equals("257210596896931840")) {
			channel.sendMessage("Kapattýk kardeþim").complete();
			
			String closed = "[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "]" + " Bot closed.\n";
			System.out.print(closed);
			try {
				if(!log.exists())
					log.createNewFile();
				
				logwriter = new FileWriter(log, true);
				logwriter.append(closed);
				logwriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			jda.shutdownNow();
			OkHttpClient client = jda.getHttpClient();
			client.connectionPool().evictAll();
			client.dispatcher().executorService().shutdown();
		} else if(msg[0].toLowerCase().equals("-karaliste") && u.getId().equals("257210596896931840")) {
			if(message.getMentionedUsers().size() == 0) {
				channel.sendMessage("Komut kullanýmý: -karaliste [hedefler]").queue();
				return;
			}
			
			List<User> tagged = message.getMentionedUsers();
			for(User t : tagged) {
				if(!blacklist.contains(t.getId())) {
					blacklist.add(t.getId());
						
					channel.sendMessage("**" + t.getName() + "** kara listeye eklendi!").queue();
					
					String added = "[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "] " + "Added " + t.getName() + " to blacklist.\n";
					System.out.print(added);
					try {
						if(!log.exists())
							log.createNewFile();
						
						logwriter = new FileWriter(log, true);
						logwriter.append(added);
						logwriter.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					blacklist.remove(t.getId());
						
					channel.sendMessage("**" + t.getName() + "** kara listeden çýkarýldý!").queue();
						
					String removed = "[" + dtf.format(ZonedDateTime.now(ZoneId.of("GMT+3"))) + "] " + "Removed " + t.getName() + " from blacklist.\n";
					System.out.print(removed);
					try {
						if(!log.exists())
							log.createNewFile();
							
						logwriter = new FileWriter(log, true);
						logwriter.append(removed);
						logwriter.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			try {
				updateBlacklist();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
