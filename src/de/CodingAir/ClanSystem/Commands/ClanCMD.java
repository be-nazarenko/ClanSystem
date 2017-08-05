package de.CodingAir.ClanSystem.Commands;

import de.CodingAir.ClanSystem.ClanSystem;
import de.CodingAir.ClanSystem.GUIs.ClanGUI;
import de.CodingAir.ClanSystem.GUIs.DepositGUI;
import de.CodingAir.ClanSystem.GUIs.WithdrawGUI;
import de.CodingAir.ClanSystem.Managers.LanguageManager;
import de.CodingAir.ClanSystem.Managers.TeleportManager;
import de.CodingAir.ClanSystem.Utils.BungeeCord.Request;
import de.CodingAir.ClanSystem.Utils.Clan;
import de.CodingAir.ClanSystem.Utils.Options;
import de.CodingAir.v1_4.CodingAPI.BungeeCord.ProxiedPlayer;
import de.CodingAir.v1_4.CodingAPI.Player.Data.UUIDFetcher;
import de.CodingAir.v1_4.CodingAPI.Player.GUI.Anvil.*;
import de.CodingAir.v1_4.CodingAPI.Tools.Callback;
import de.CodingAir.v1_4.CodingAPI.Tools.ItemBuilder;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClanCMD implements CommandExecutor, TabCompleter {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!ClanSystem.isInited()) return false;
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(LanguageManager.SUBPREFIX.getMessage() + LanguageManager.ONLY_FOR_PLAYERS.getMessage());
			return false;
		}
		
		Player p = (Player) sender;
		
		if(!ClanSystem.getInstance().getBungeeCordManager().isConnected() && Options.BUNGEECORD.getBoolean()) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_COMMANDS_BLOCKED.getMessage());
			return false;
		}
		
		if(!ClanSystem.isRegistered(p)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_PLAYER_NOT_REGISTERED.getMessage());
			return false;
		}
		
		if(args.length == 0) {
			if(ClanSystem.getClanManager().getClan(p) != null) ClanGUI.MAIN.open(p);
			else {
				ClanGUI.CONFIRM.open(p, LanguageManager.GUI_CLAN_APPLY.getMessage(), new Callback<Boolean>() {
					@Override
					public void accept(Boolean create) {
						if(create) {
							AnvilGUI.openAnvil(ClanSystem.getInstance(), p, new AnvilListener() {
								@Override
								public void onClick(AnvilClickEvent e) {
									e.setCancelled(true);
									e.setClose(false);
									
									System.out.println("Click!");
									
									if(e.getSlot().equals(AnvilSlot.OUTPUT)) {
										String input = e.getInput();
										
										if(input == null || input.isEmpty()) {
											p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.GUI_ENTER_A_NAME.getMessage());
											return;
										}
										
										if(ClanSystem.getClanManager().exists(input)) {
											p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_CLAN_ALREADY_EXISTS.getMessage());
											return;
										}
										
										if(input.length() > Options.CLAN_NAME_LENGTH.getInt()) {
											p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_CLAN_NAME_TO_LONG.getMessage().replace("%num_of_chars%", Options.CLAN_NAME_LENGTH.getInt() + ""));
											return;
										}
										
										ClanSystem.getClanManager().createClan(p, input);
										p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.SUCCESS_CLAN_CREATED.getMessage().replace("%clanname%", input));
										p.closeInventory();
									}
								}
								
								@Override
								public void onClose(AnvilCloseEvent e) {
									
								}
							}, ItemBuilder.getItem(Material.PAPER, LanguageManager.GUI_NAME.getMessage() + "..."), null);
						}
					}
				});
			}
			return true;
		}
		
		String sub = args[0];
		
		if(sub.equalsIgnoreCase("Plugin")) {
			p.sendMessage("");
			p.sendMessage(ClanSystem.HEADER_SHORT);
			p.sendMessage("  §3Author§8: §b§lCodingAir");
			p.sendMessage("  §3Version§8: §b" + ClanSystem.getInstance().getDescription().getVersion());
			p.sendMessage("  ");
			p.sendMessage("  §7Need help? Try §b/" + label + " " + LanguageManager.COMMANDS_HELP.getMessage());
			return true;
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_HELP.getMessage())) {
			return help(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_CREATE.getMessage())) {
			return create(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_DELETE.getMessage())) {
			return delete(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_LEAVE.getMessage())) {
			return leave(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_INVITE.getMessage())) {
			return invite(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_KICK.getMessage())) {
			return kick(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_INFO.getMessage())) {
			return info(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_CHAT.getMessage())) {
			return chat(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_ALLIANCE.getMessage())) {
			return alliance(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_NEUTRAL.getMessage())) {
			return neutral(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_DEMOTE.getMessage())) {
			return demote(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_PROMOTE.getMessage())) {
			return promote(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_LEADER.getMessage())) {
			return leader(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_BASE.getMessage()) && Options.CLAN_BASES.getBoolean()) {
			return base(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_SET_BASE.getMessage()) && Options.CLAN_BASES.getBoolean()) {
			return setBase(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_DEPOSIT.getMessage()) && Options.ECONOMY_ENABLED.getBoolean()) {
			return deposit(p, label, args);
		}
		
		if(sub.equalsIgnoreCase(LanguageManager.COMMANDS_WITHDRAW.getMessage()) && Options.ECONOMY_ENABLED.getBoolean()) {
			return withdraw(p, label, args);
		}
		
		p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_UNKNOWN_COMMAND.getMessage().replace("%label%", label));
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> suggestions = new ArrayList<>();
		
		if(!(sender instanceof Player)) {
			return suggestions;
		}
		
		Player p = (Player) sender;
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(args.length == 1) {
			for(LanguageManager value : LanguageManager.values()) {
				if(value.name().startsWith("COMMANDS_") && !value.name().equalsIgnoreCase("COMMANDS_ACCEPT") && !value.name().equalsIgnoreCase("COMMANDS_DECLINE")
						&& !value.name().equalsIgnoreCase("COMMANDS_CLAN_CHAT")) {
					
					if((value.name().toLowerCase().contains("withdraw") || value.name().toLowerCase().contains("deposit")) && !Options.ECONOMY_ENABLED.getBoolean())
						continue;
					
					if(value.name().toLowerCase().contains("base") && !Options.CLAN_BASES.getBoolean())
						continue;
					
					if(args[0] == null) suggestions.add(value.getMessage());
					else {
						if(value.getMessage().toLowerCase().startsWith(args[0].toLowerCase()))
							suggestions.add(value.getMessage());
					}
				}
			}
		} else if(args.length == 2) {
			if(args[0].equalsIgnoreCase(LanguageManager.COMMANDS_HELP.getMessage())) {
				suggestions.add("1");
				suggestions.add("2");
			}
			
			if(clan != null && args[0].equalsIgnoreCase(LanguageManager.COMMANDS_INVITE.getMessage())) {
				List<String> online = new ArrayList<>();
				
				Bukkit.getOnlinePlayers().forEach(all -> online.add(all.getName()));
				if(Options.BUNGEECORD.getBoolean())
					ClanSystem.getInstance().getBungeeCordManager().getProxiedPlayers().forEach(all -> online.add(all.getName()));
				
				clan.getAllMembers().forEach((member, uniqueId) -> {
					if(online.contains(member)) online.remove(member);
				});
				
				suggestions.addAll(online);
			}
			
			if(clan != null && args[0].equalsIgnoreCase(LanguageManager.COMMANDS_KICK.getMessage())) {
				clan.getMembers().forEach((name, uID) -> suggestions.add(name));
				clan.getTrusted().forEach((name, uID) -> suggestions.add(name));
			}
			
			if(args[0].equalsIgnoreCase(LanguageManager.COMMANDS_INFO.getMessage()))
				ClanSystem.getClanManager().getClans().forEach(c -> suggestions.add(c.getName()));
			
			if(clan != null && args[0].equalsIgnoreCase(LanguageManager.COMMANDS_CHAT.getMessage())) {
				suggestions.add("On");
				suggestions.add("Off");
			}
			
			if(clan != null && args[0].equalsIgnoreCase(LanguageManager.COMMANDS_ALLIANCE.getMessage()))
				ClanSystem.getClanManager().getClans().forEach(c -> suggestions.add(c.getName()));
			
			if(clan != null && args[0].equalsIgnoreCase(LanguageManager.COMMANDS_NEUTRAL.getMessage()))
				suggestions.addAll(clan.getAlliances());
			
			if(clan != null && args[0].equalsIgnoreCase(LanguageManager.COMMANDS_DEMOTE.getMessage()))
				clan.getTrusted().forEach((name, uID) -> suggestions.add(name));
			
			if(clan != null && args[0].equalsIgnoreCase(LanguageManager.COMMANDS_PROMOTE.getMessage()))
				clan.getMembers().forEach((name, uID) -> suggestions.add(name));
			
			if(clan != null && args[0].equalsIgnoreCase(LanguageManager.COMMANDS_LEADER.getMessage())) {
				clan.getMembers().forEach((name, uID) -> suggestions.add(name));
				clan.getTrusted().forEach((name, uID) -> suggestions.add(name));
			}
			
		}
		
		return suggestions;
	}
	
	public boolean help(Player p, String label, String[] args) {
		int page;
		
		if(args.length != 2) {
			page = 1;
		} else {
			try {
				page = Integer.parseInt(args[1]);
			} catch(NumberFormatException ex) {
				p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NUMBER_REQUIRED.getMessage());
				return false;
			}
			
			if(page != 1 && page != 2) {
				p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_UNKNOWN_PAGE.getMessage());
				return false;
			}
		}
		
		p.sendMessage("");
		p.sendMessage(ClanSystem.HEADER);
		
		if(page == 1) {
			LanguageManager.HELP_OUTPUT_PAGE_1.getStringList().forEach(msg -> p.sendMessage(msg));
		} else {
			LanguageManager.HELP_OUTPUT_PAGE_2.getStringList().forEach(msg -> p.sendMessage(msg));
		}
		
		return true;
	}
	
	private boolean create(Player p, String label, String[] args) {
		if(ClanSystem.getClanManager().getClan(p) != null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_PLAYER_IS_ALREADY_IN_A_CLAN.getMessage());
			return false;
		}
		
		if(args.length != 2) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_CREATE.getMessage().replace("%label%", label));
			return false;
		}
		
		String name = args[1];
		
		if(ClanSystem.getClanManager().exists(name)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_CLAN_ALREADY_EXISTS.getMessage());
			return false;
		}
		
		if(name.length() > Options.CLAN_NAME_LENGTH.getInt()) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_CLAN_NAME_TO_LONG.getMessage().replace("%num_of_chars%", Options.CLAN_NAME_LENGTH.getInt() + ""));
			return false;
		}
		
		ClanSystem.getClanManager().createClan(p, name);
		p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.SUCCESS_CLAN_CREATED.getMessage().replace("%clanname%", name));
		
		return true;
	}
	
	private boolean delete(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(!clan.isLeader(p)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NO_LEADER.getMessage());
			return false;
		}
		
		ClanGUI.CONFIRM.open(p, LanguageManager.GUI_DELETE_APPLY.getMessage(), new Callback<Boolean>() {
			@Override
			public void accept(Boolean deleted) {
				if(deleted) {
					clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.CLAN_DELETED_BY_LEADER.getMessage(), p);
					clan.kickAll();
					ClanSystem.getClanManager().removeClan(clan);
					
					p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.SUCCESS_CLAN_DELETED.getMessage().replace("%clanname%", clan.getName()));
				} else {
					p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_CLAN_NOT_DELETED.getMessage());
				}
			}
		});
		
		return true;
	}
	
	private boolean leave(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(clan.isLeader(p)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_LEADERS_CAN_NOT_LEAVE.getMessage());
			return false;
		}
		
		ClanGUI.CONFIRM.open(p, LanguageManager.GUI_LEAVE.getMessage(), new Callback<Boolean>() {
			@Override
			public void accept(Boolean leave) {
				if(leave) {
					clan.kick(p);
					p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.SUCCESS_LEAVE.getMessage().replace("%clanname%", clan.getName()));
					
					clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.CLAN_PLAYER_LEAVE.getMessage().replace("%player%", p.getName()));
				} else {
					p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_CLAN_NOT_LEAVED.getMessage());
				}
			}
		});
		
		return true;
	}
	
	private boolean invite(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(args.length == 3) {
			if(args[1].equalsIgnoreCase(LanguageManager.COMMANDS_ACCEPT.getMessage())) {
				return invite_accept(p, label, args);
			}
			
			if(args[1].equalsIgnoreCase(LanguageManager.COMMANDS_DECLINE.getMessage())) {
				return invite_decline(p, label, args);
			}
		}
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(clan.getSize() >= Options.CLAN_SIZE.getInt()) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_CLAN_ALREADY_FULL.getMessage());
			return false;
		}
		
		if(args.length != 2) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_INVITE.getMessage().replace("%label%", label));
			return false;
		}
		
		if(!clan.isLeader(p) && (!ClanSystem.getClanManager().trustedCanInvite() || !clan.isTrusted(p))) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NO_PERMISSION.getMessage());
			return false;
		}
		
		if(p.getName().equalsIgnoreCase(args[1])) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_SELF_INVITATION.getMessage());
			return false;
		}
		
		Player other = Bukkit.getPlayer(args[1]);
		ProxiedPlayer proxy = ClanSystem.getInstance().getBungeeCordManager().getProxiedPlayer(args[1]);
		
		if(other == null && proxy == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_PLAYER_IS_OFFLINE.getMessage());
			return false;
		}
		
		UUID uniqueId = (other == null ? proxy.getUniqueId() : ClanSystem.getUUID(other));
		
		if(ClanSystem.getClanManager().getClan(uniqueId) != null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_THIS_PLAYER_IS_ALREADY_IN_A_CLAN.getMessage());
			return false;
		}
		
		if(ClanSystem.getClanManager().hasInvite(uniqueId, clan)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_PLAYER_HAS_ALREADY_A_INVITATION.getMessage());
			return false;
		}
		
		if(other == null && Options.BUNGEECORD.getBoolean()) {
			ClanSystem.getInstance().getBungeeCordManager().request(new Request(Request.Type.INVITE_PLAYER, clan.getName(), proxy.getName()));
			return true;
		}
		
		ClanSystem.getClanManager().invite(clan, uniqueId);
		
		String msg = LanguageManager.PREFIX.getMessage() + LanguageManager.CLAN_PLAYER_INVITE.getMessage().replace("%clan%", clan.getName());
		
		if(msg.contains("%yes%") && msg.contains("%/yes%") && msg.contains("%no%") && msg.contains("%/no%")) {
			String yes = msg.split("%yes%")[1].split("%/yes%")[0];
			String no = msg.split("%no%")[1].split("%/no%")[0];
			
			TextComponent message = new TextComponent(msg.split("%yes%")[0]);
			
			TextComponent accept = new TextComponent(yes);
			accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan " + LanguageManager.COMMANDS_INVITE.getMessage() + " " + LanguageManager.COMMANDS_ACCEPT.getMessage() + " " + clan.getName()));
			accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(LanguageManager.GUI_CLICK.getMessage()).create()));
			
			TextComponent decline = new TextComponent(no);
			decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan " + LanguageManager.COMMANDS_INVITE.getMessage() + " " + LanguageManager.COMMANDS_DECLINE.getMessage() + " " + clan.getName()));
			decline.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(LanguageManager.GUI_CLICK.getMessage()).create()));
			
			message.addExtra(accept);
			message.addExtra(msg.split("%/yes%")[1].split("%no%")[0]);
			message.addExtra(decline);
			message.addExtra(msg.split("%/no%")[1]);
			
			other.spigot().sendMessage(message);
		} else {
			other.sendMessage(msg);
		}
		
		clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.CLAN_PLAYER_INVITE_NOTIFY.getMessage().replace("%player%", other.getName()));
		
		return true;
	}
	
	private boolean invite_accept(Player p, String label, String[] args) {
		if(ClanSystem.getClanManager().getClan(p) != null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_PLAYER_IS_ALREADY_IN_A_CLAN.getMessage());
			return false;
		}
		
		if(args.length != 3) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_INVITE_ACCEPT.getMessage().replace("%label%", label));
			return false;
		}
		
		Clan clan = ClanSystem.getClanManager().getClan(args[2]);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		System.out.println(!ClanSystem.getClanManager().hasInvite(p, clan)
				+ " " + (ClanSystem.getClanManager().getInvite(p, clan) == null)
				+ " " + (ClanSystem.getClanManager().getInvite(p, clan) == null || ClanSystem.getClanManager().getInvite(p, clan).isExpired()));
		
		if(!ClanSystem.getClanManager().hasInvite(p, clan) || ClanSystem.getClanManager().getInvite(p, clan) == null || ClanSystem.getClanManager().getInvite(p, clan).isExpired()) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_INVITE.getMessage());
			return false;
		}
		
		if(clan.getSize() >= Options.CLAN_SIZE.getInt()) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_THIS_CLAN_IS_ALREADY_FULL.getMessage());
			return false;
		}
		
		clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.CLAN_PLAYER_JOIN.getMessage().replace("%player%", p.getName()));
		clan.add(p);
		
		p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.SUCCESS_JOIN.getMessage().replace("%clanname%", clan.getName()));
		
		ClanSystem.getClanManager().removeInvite(p, clan);
		
		return true;
	}
	
	private boolean invite_decline(Player p, String label, String[] args) {
		if(ClanSystem.getClanManager().getClan(p) != null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_PLAYER_IS_ALREADY_IN_A_CLAN.getMessage());
			return false;
		}
		
		if(args.length != 3) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_INVITE_DECLINE.getMessage().replace("%label%", label));
			return false;
		}
		
		Clan clan = ClanSystem.getClanManager().getClan(args[2]);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(!ClanSystem.getClanManager().hasInvite(p, clan) || ClanSystem.getClanManager().getInvite(p, clan).isExpired()) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_INVITE.getMessage());
			return false;
		}
		
		if(clan.getSize() >= Options.CLAN_SIZE.getInt()) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_THIS_CLAN_IS_ALREADY_FULL.getMessage());
			return false;
		}
		
		clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.CLAN_INVITATION_DECLINE_CLAN.getMessage().replace("%player%", p.getName()));
		p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.CLAN_INVITATION_DECLINE_PLAYER.getMessage());
		ClanSystem.getClanManager().removeInvite(p, clan);
		
		return true;
	}
	
	private boolean kick(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(!clan.isLeader(p) && (!ClanSystem.getClanManager().trustedCanKick() || !clan.isTrusted(p))) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NO_PERMISSION.getMessage());
			return false;
		}
		
		if(args.length != 2) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_KICK.getMessage().replace("%label%", label));
			return false;
		}
		
		final String name;
		Player other = Bukkit.getPlayer(args[1]);
		UUID target;
		
		if(other == null) {
			name = args[1];
			
			UUIDFetcher.getUUID(name, new Callback<UUID>() {
				@Override
				public void accept(UUID target) {
					if(target == null) {
						p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_PLAYER_NOT_EXISTS.getMessage());
					} else {
						if(!clan.isMember(target)) {
							p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_OTHER_CLAN.getMessage());
							return;
						}
						
						if(clan.isLeader(target)) {
							p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_LEADERS_CAN_NOT_BE_KICKED.getMessage());
							return;
						}
						
						clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.CLAN_KICK.getMessage().replace("%player%", name).replace("%stuff%", p.getName()).replace("%staff%", p.getName()));
						clan.kick(target);
					}
				}
			});
			
			return true;
		} else {
			target = other.getUniqueId();
			name = other.getName();
		}
		
		if(!clan.isMember(target)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_OTHER_CLAN.getMessage());
			return false;
		}
		
		if(other.getName().equals(p.getName())) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_SELF_KICK.getMessage());
			return false;
		}
		
		if(clan.isLeader(target)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_LEADERS_CAN_NOT_BE_KICKED.getMessage());
			return false;
		}
		
		clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.CLAN_KICK.getMessage().replace("%player%", name).replace("%stuff%", p.getName()));
		clan.kick(target);
		
		return true;
	}
	
	private boolean info(Player p, String label, String[] args) {
		final Clan clan;
		
		if(args.length != 2) {
			clan = ClanSystem.getClanManager().getClan(p);
			
			if(clan == null) {
				p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
				return false;
			}
		} else {
			String name = args[1];
			clan = ClanSystem.getClanManager().getClan(name);
			
			if(clan == null) {
				p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_CLAN_NOT_EXISTS.getMessage());
				return false;
			}
		}
		
		List<String> out = LanguageManager.INFO_OUTPUT.getStringList();
		List<String> members = new ArrayList<>();
		List<String> trusted = new ArrayList<>();
		
		clan.getMembers().forEach((name, uID) -> members.add(LanguageManager.INFO_FORMAT_MEMBER_LIST.getMessage().replace("%player%", name)));
		clan.getTrusted().forEach((name, uID) -> trusted.add(LanguageManager.INFO_FORMAT_TRUSTED_LIST.getMessage().replace("%player%", name)));
		String empty = LanguageManager.INFO_FORMAT_EMPTY_LIST.getMessage();
		
		p.sendMessage("");
		p.sendMessage(ClanSystem.HEADER_SHORT);
		
		double kd = ((double) ((int) (((double) clan.getKills()) / ((double) (clan.getDeaths() == 0 ? 1 : clan.getDeaths())) * 100)) / 100);
		
		out.forEach(msg -> {
			if(!msg.contains("%money%") || Options.ECONOMY_ENABLED.getBoolean()) {
				msg = msg.replace("%clanname%", clan.getName());
				msg = msg.replace("%leader%", clan.getLeader());
				msg = msg.replace("%kills%", clan.getKills() + "");
				msg = msg.replace("%deaths%", clan.getDeaths() + "");
				msg = msg.replace("%kd%", kd + "");
				msg = msg.replace("%clan_rank%", clan.getClanRank() + "");
				msg = msg.replace("%money%", clan.getBalance() + "");
				
				if(msg.contains("%trustedlist%")) {
					msg = msg.replace("%trustedlist%", (trusted.size() == 0 ? empty : ""));
					p.sendMessage(msg);
					trusted.forEach(user -> p.sendMessage(user));
				} else if(msg.contains("%memberlist%")) {
					msg = msg.replace("%memberlist%", (members.size() == 0 ? empty : ""));
					p.sendMessage(msg);
					members.forEach(user -> p.sendMessage(user));
				} else {
					p.sendMessage(msg);
				}
			}
		});
		
		return true;
	}
	
	private boolean chat(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(!clan.isLeader(p) && (!ClanSystem.getClanManager().trustedCanToggleChat() || !clan.isTrusted(p))) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NO_PERMISSION.getMessage());
			return false;
		}
		
		if(args.length != 2) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_CHAT.getMessage().replace("%label%", label));
			return false;
		}
		
		String state = args[1];
		
		if(state.equalsIgnoreCase(LanguageManager.ON.getMessage())) {
			clan.setChat(true);
			clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.SUCCESS_CLAN_CHAT_ENABLED.getMessage());
		} else if(state.equalsIgnoreCase(LanguageManager.OFF.getMessage())) {
			clan.setChat(false);
			clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.SUCCESS_CLAN_CHAT_DISABLED.getMessage());
		} else {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_CHAT.getMessage().replace("%label%", label));
		}
		
		return true;
	}
	
	private boolean alliance(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(args.length == 3) {
			if(args[1].equalsIgnoreCase(LanguageManager.COMMANDS_ACCEPT.getMessage())) {
				return alliance_accept(p, label, args);
			}
			
			if(args[1].equalsIgnoreCase(LanguageManager.COMMANDS_DECLINE.getMessage())) {
				return alliance_decline(p, label, args);
			}
		}
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(clan.getSize() >= Options.CLAN_MAX_ALLIANCES.getInt()) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_CLAN_ALREADY_FULL.getMessage());
			return false;
		}
		
		if(!clan.isLeader(p) && (!ClanSystem.getClanManager().trustedCanAlliance() || !clan.isTrusted(p))) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NO_PERMISSION.getMessage());
			return false;
		}
		
		if(args.length != 2) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_ALLIANCE.getMessage().replace("%label%", label));
			return false;
		}
		
		Clan target = ClanSystem.getClanManager().getClan(args[1]);
		
		if(target == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_CLAN_NOT_EXISTS.getMessage());
			return false;
		}
		
		if(target.getName().equals(clan.getName())) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_OWN_CLAN.getMessage());
			return false;
		}
		
		if(clan.hasAllianceWith(target)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_ALREADY_IN_ALLIANCE.getMessage().replace("%clan%", target.getName()));
			return false;
		}
		
		Player targetLeader = Bukkit.getPlayer(target.getLeader());
		ProxiedPlayer proxy = ClanSystem.getInstance().getBungeeCordManager().getProxiedPlayer(target.getLeader());
		
		if(targetLeader == null && proxy == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_LEADER_NOT_ONLINE.getMessage());
			return false;
		}
		
		if(targetLeader == null && Options.BUNGEECORD.getBoolean()) {
			ClanSystem.getInstance().getBungeeCordManager().request(new Request(Request.Type.INVITE_CLAN, clan.getName(), target.getName(), proxy.getName()));
			return true;
		}
		
		String msg = LanguageManager.PREFIX.getMessage() + LanguageManager.CLAN_ALLIANCE_TARGET.getMessage().replace("%clan%", clan.getName());
		
		if(msg.contains("%yes%") && msg.contains("%/yes%") && msg.contains("%no%") && msg.contains("%/no%")) {
			String yes = msg.split("%yes%")[1].split("%/yes%")[0];
			String no = msg.split("%no%")[1].split("%/no%")[0];
			
			TextComponent message = new TextComponent(msg.split("%yes%")[0]);
			
			TextComponent accept = new TextComponent(yes);
			accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan " + LanguageManager.COMMANDS_ALLIANCE.getMessage() + " " + LanguageManager.COMMANDS_ACCEPT.getMessage() + " " + clan.getName()));
			accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(LanguageManager.GUI_CLICK.getMessage()).create()));
			
			TextComponent decline = new TextComponent(no);
			decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan " + LanguageManager.COMMANDS_ALLIANCE.getMessage() + " " + LanguageManager.COMMANDS_DECLINE.getMessage() + " " + clan.getName()));
			decline.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(LanguageManager.GUI_CLICK.getMessage()).create()));
			
			message.addExtra(accept);
			message.addExtra(msg.split("%/yes%")[1].split("%no%")[0]);
			message.addExtra(decline);
			message.addExtra(msg.split("%/no%")[1]);
			
			targetLeader.spigot().sendMessage(message);
		} else {
			targetLeader.sendMessage(msg);
		}
		
		ClanSystem.getClanManager().alliance(clan, target);
		clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.CLAN_ALLIANCE_CLAN.getMessage().replace("%clan%", target.getName()));
		
		return true;
	}
	
	private boolean alliance_accept(Player p, String label, String[] args) {
		if(ClanSystem.getClanManager().getClan(p) == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		Clan target = ClanSystem.getClanManager().getClan(p);
		
		if(!target.isLeader(p)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NO_LEADER.getMessage());
			return false;
		}
		
		if(target.getAlliances().size() >= Options.CLAN_MAX_ALLIANCES.getInt()) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_CLAN_MAX_ALLIANCES.getMessage());
			return false;
		}
		
		if(args.length != 3) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_ALLIANCE_ACCEPT.getMessage().replace("%label%", label));
			return false;
		}
		
		Clan clan = ClanSystem.getClanManager().getClan(args[2]);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(!ClanSystem.getClanManager().hasAllianceInvite(target, clan) || ClanSystem.getClanManager().getAllianceInvite(target, clan).isExpired()) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_ALLIANCE_INVITE.getMessage());
			return false;
		}
		
		clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.CLAN_CLAN_JOINED_ALLIANCE.getMessage().replace("%clan%", target.getName()));
		target.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(target.getClanRank())).replace("%clanname%", target.getName()) + LanguageManager.SUCCESS_CLAN_JOINED_ALLIANCE.getMessage().replace("%clan%", target.getName()).replace("%target%", clan.getName()));
		
		clan.addAlliance(target);
		target.addAlliance(clan);
		
		ClanSystem.getClanManager().removeAllianceInvite(target, clan);
		
		return true;
	}
	
	private boolean alliance_decline(Player p, String label, String[] args) {
		if(ClanSystem.getClanManager().getClan(p) == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		Clan target = ClanSystem.getClanManager().getClan(p);
		
		if(!target.isLeader(p)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NO_LEADER.getMessage());
			return false;
		}
		
		if(args.length != 3) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_ALLIANCE_DECLINE.getMessage().replace("%label%", label));
			return false;
		}
		
		Clan clan = ClanSystem.getClanManager().getClan(args[2]);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(!ClanSystem.getClanManager().hasAllianceInvite(target, clan) || ClanSystem.getClanManager().getAllianceInvite(target, clan).isExpired()) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_ALLIANCE_INVITE.getMessage());
			return false;
		}
		
		clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.CLAN_ALLIANCE_DECLINED_ClAN.getMessage().replace("%clan%", target.getName()));
		target.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(target.getClanRank())).replace("%name%", target.getName()) + LanguageManager.CLAN_ALLIANCE_DECLINED_TARGET.getMessage().replace("%clan%", clan.getName()));
		
		ClanSystem.getClanManager().removeAllianceInvite(target, clan);
		
		return true;
	}
	
	private boolean neutral(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(!clan.isLeader(p) && (!ClanSystem.getClanManager().trustedCanNeutral() || !clan.isTrusted(p))) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NO_PERMISSION.getMessage());
			return false;
		}
		
		if(args.length != 2) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_NEUTRAL.getMessage().replace("%label%", label));
			return false;
		}
		
		Clan target = ClanSystem.getClanManager().getClan(args[1]);
		
		if(target == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_CLAN_NOT_EXISTS.getMessage());
			return false;
		}
		
		if(!clan.hasAllianceWith(target)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_ALLIANCE.getMessage().replace("%clan%", target.getName()));
			return false;
		}
		
		clan.removeAlliance(target);
		target.removeAlliance(clan);
		
		clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.SUCCESS_ALLIANCE_REMOVED.getMessage().replace("%clan%", target.getName()));
		target.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(target.getClanRank())).replace("%clanname%", target.getName()) + LanguageManager.SUCCESS_ALLIANCE_REMOVED.getMessage().replace("%clan%", clan.getName()));
		
		return true;
	}
	
	private boolean demote(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(!clan.isLeader(p) && (!ClanSystem.getClanManager().trustedCanDemote() || !clan.isTrusted(p))) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NO_PERMISSION.getMessage());
			return false;
		}
		
		if(args.length != 2) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_LEADER.getMessage().replace("%label%", label));
			return false;
		}
		
		String name = args[1];
		Player target = Bukkit.getPlayer(name);
		
		if(target == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_PLAYER_IS_OFFLINE.getMessage());
			return false;
		}
		
		Clan targetClan = ClanSystem.getClanManager().getClan(target);
		
		if(!clan.equals(targetClan)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_OTHER_CLAN.getMessage());
			return false;
		}
		
		if(target.getName().equals(p.getName())) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_DEMOTE_WRONG_COMMAND.getMessage());
			return false;
		}
		
		if(!clan.isTrusted(target)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_ALREADY_MEMBER.getMessage().replace("%rank_color%", ClanSystem.getClanManager().getColor(2)));
			return false;
		}
		
		clan.setTrusted(target, false);
		clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.SUCCESS_DEMOTE.getMessage().replace("%player%", target.getName()).replace("%rank_color%", ClanSystem.getClanManager().getColor(2)));
		
		return true;
	}
	
	private boolean promote(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(!clan.isLeader(p) && (!ClanSystem.getClanManager().trustedCanPromote() || !clan.isTrusted(p))) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NO_PERMISSION.getMessage());
			return false;
		}
		
		if(args.length != 2) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_LEADER.getMessage().replace("%label%", label));
			return false;
		}
		
		String name = args[1];
		Player target = Bukkit.getPlayer(name);
		
		if(target == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_PLAYER_IS_OFFLINE.getMessage());
			return false;
		}
		
		Clan targetClan = ClanSystem.getClanManager().getClan(target);
		
		if(!clan.equals(targetClan)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_OTHER_CLAN.getMessage());
			return false;
		}
		
		if(target.getName().equals(p.getName())) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_DEMOTE_WRONG_COMMAND.getMessage());
			return false;
		}
		
		if(clan.isTrusted(target)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_ALREADY_TRUSTED.getMessage().replace("%rank_color%", ClanSystem.getClanManager().getColor(1)));
			return false;
		}
		
		clan.setTrusted(target, true);
		clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.SUCCESS_PROMOTE.getMessage().replace("%player%", target.getName()).replace("%rank_color%", ClanSystem.getClanManager().getColor(1)));
		
		return true;
	}
	
	private boolean leader(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(!clan.isLeader(p)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NO_PERMISSION.getMessage());
			return false;
		}
		
		if(args.length != 2) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.HELP_LEADER.getMessage().replace("%label%", label));
			return false;
		}
		
		String name = args[1];
		Player target = Bukkit.getPlayer(name);
		
		if(target == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_PLAYER_IS_OFFLINE.getMessage());
			return false;
		}
		
		Clan targetClan = ClanSystem.getClanManager().getClan(target);
		
		if(!clan.equals(targetClan)) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_OTHER_CLAN.getMessage());
			return false;
		}
		
		if(target.getName().equals(p.getName())) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_ALREADY_LEADER.getMessage());
			return false;
		}
		
		ClanGUI.CONFIRM.open(p, LanguageManager.GUI_LEADER_APPLY.getMessage().replace("%player%", name), new Callback<Boolean>() {
			@Override
			public void accept(Boolean accepted) {
				if(accepted) {
					clan.kick(target);
					clan.setLeader(target.getName());
					clan.setLeader_uuid(target.getUniqueId().toString());
					clan.add(p);
					clan.setTrusted(p, true);
					clan.broadcast(LanguageManager.CLAN_PREFIX.getMessage().replace("%clan_color%", ClanSystem.getClanManager().getClanColor(clan.getClanRank())).replace("%clanname%", clan.getName()) + LanguageManager.SUCCESS_NEW_LEADER.getMessage().replace("%player%", target.getName()));
				} else {
					p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_LEADER_NOT_SET.getMessage());
				}
			}
		});
		
		return true;
	}
	
	private boolean base(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(clan.getHomeServer() == null || clan.getHomeServer().isEmpty()) {
			clan.setHomeServer(ClanSystem.SERVER);
		}
		
		if(!clan.rightServer()) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_WRONG_SERVER.getMessage().replace("%server%", clan.getHomeServer()));
			return false;
		}
		
		if(clan.getBase() == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN_BASE.getMessage());
			return false;
		}
		
		TeleportManager.teleport(p, clan.getBase());
		
		return true;
	}
	
	private boolean setBase(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(!clan.isLeader(p) && (!ClanSystem.getClanManager().trustedCanSetBase() || !clan.isTrusted(p))) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NO_PERMISSION.getMessage());
			return false;
		}
		
		clan.setBase(p.getLocation());
		p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.SUCCESS_BASE_SET.getMessage());
		return true;
	}
	
	private boolean deposit(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		DepositGUI.openInterface(p);
		
		return true;
	}
	
	private boolean withdraw(Player p, String label, String[] args) {
		Clan clan = ClanSystem.getClanManager().getClan(p);
		
		if(clan == null) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.ERROR_NO_CLAN.getMessage());
			return false;
		}
		
		if(!clan.isLeader(p) && (!Options.TRUSTED_PERMISSIONS_WITHDRAW.getBoolean() || !clan.isTrusted(p))) {
			p.sendMessage(LanguageManager.PREFIX.getMessage() + LanguageManager.NO_PERMISSION.getMessage());
			return false;
		}
		
		WithdrawGUI.openInterface(p);
		
		return true;
	}
}
