package com.lcm.regeneration.util;

import com.lcm.regeneration.superpower.TimelordSuperpowerHandler;

import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CmdRegenDebug extends CommandBase {
	
	@Override
	public String getName() {
		return "regdebug";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return "/regdebug <actionindex>";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		for (int i=0; i<args.length; i++) { //@formatter:off
			int c;
			try { c = Integer.parseInt(args[i]); }
			catch (Exception e) { throw new CommandException("Failed to execute debug action " + args[i], e); } //@formatter:on
			@SuppressWarnings("unused")
			EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
			
			switch (c) {
				case 0:
					SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, TimelordSuperpowerHandler.class).regenerationsLeft = Integer.valueOf(args[i+1]);
					i++;
					break;
				default:
					server.sendMessage(new TextComponentString("No debug action defined for " + c));
			}
		}
	}
	
}
