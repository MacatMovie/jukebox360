package com.macatmovie.jukebox360.client.gui;

import com.macatmovie.jukebox360.config.ClientConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public final class BlockWhitelistScreen extends Screen {
    private static final int PAGE_SIZE=5; private final Screen parent; private final List<String> entries; private final int page; private EditBox input;
    public BlockWhitelistScreen(Screen parent){this(parent,ClientConfig.blockWhitelist(),0);}
    private BlockWhitelistScreen(Screen parent,List<String> entries,int page){super(Component.literal("Jukebox 360 Audio Source Blocks"));this.parent=parent;this.entries=new ArrayList<>(entries);this.page=Math.max(0,page);}
    @Override protected void init(){int x=width/2-150,y=28; input=new EditBox(font,x,y,220,20,Component.literal("modid:block"));input.setMaxLength(160);addRenderableWidget(input); addRenderableWidget(new Button(x+226,y,74,20,Component.literal("Add"),b->addEntry())); int start=page*PAGE_SIZE,end=Math.min(entries.size(),start+PAGE_SIZE); for(int i=start;i<end;i++){final int idx=i;String id=entries.get(i);addRenderableWidget(new Button(x,y+30+(i-start)*24,300,20,Component.literal("Remove: "+id),b->removeEntry(idx)));} int navY=y+30+PAGE_SIZE*24+4; Button prev=addRenderableWidget(new Button(x,navY,96,20,Component.literal("< Previous"),b->openPage(Math.max(0,page-1))));prev.active=page>0;int max=entries.isEmpty()?0:(entries.size()-1)/PAGE_SIZE;Button next=addRenderableWidget(new Button(x+102,navY,96,20,Component.literal("Next >"),b->openPage(Math.min(max,page+1))));next.active=page<max;addRenderableWidget(new Button(x+204,navY,96,20,Component.literal("Reset List"),b->minecraft.setScreen(new BlockWhitelistScreen(parent,ClientConfig.DEFAULT_BLOCK_WHITELIST,0))));addRenderableWidget(new Button(width/2-75,navY+28,150,20,Component.literal("Done"),b->saveAndClose()));}
    private void addEntry(){String id=input.getValue().trim().toLowerCase();if(!ClientConfig.isValidId(id)||entries.contains(id))return;entries.add(id);minecraft.setScreen(new BlockWhitelistScreen(parent,entries,(entries.size()-1)/PAGE_SIZE));}
    private void removeEntry(int i){if(i>=0&&i<entries.size())entries.remove(i);int m=entries.isEmpty()?0:(entries.size()-1)/PAGE_SIZE;minecraft.setScreen(new BlockWhitelistScreen(parent,entries,Math.min(page,m)));}
    private void openPage(int p){minecraft.setScreen(new BlockWhitelistScreen(parent,entries,p));}
    private void saveAndClose(){ClientConfig.setBlockWhitelist(entries);ClientConfig.save();minecraft.setScreen(parent);}
    @Override public void onClose(){saveAndClose();}
}
