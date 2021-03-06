/*
 * Vulcan Build Manager
 * Copyright (C) 2005-2012 Chris Eldredge
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.sourceforge.vulcan.jabber;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.vulcan.event.ErrorEvent;
import net.sourceforge.vulcan.event.EventHandler;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

public class XmppClient implements JabberClient, MessageListener {
	private static final Log LOG = LogFactory.getLog(XmppClient.class);
	
	private final Object lock = new Object();
	private EventHandler eventHandler;
	private String connectionString;
	protected XMPPConnection connection;
	private final List<JabberChatListener> listeners = new ArrayList<JabberChatListener>();
	
	@Override
	public void refreshConnection(JabberPluginConfig config) {
		String password = config.getPassword();
		if (password == null) {
			password = StringUtils.EMPTY;
		}
		final String connectionString = config.getServer() + ":" + config.getPort() + ":" + config.getServiceName() + ":" + config.getUsername() + ":" + DigestUtils.shaHex(password);
		
		synchronized(lock) {
			if (connectionString.equals(this.connectionString) && connection != null && connection.isConnected()) {
				return;
			}
		
			this.connectionString = connectionString;
			
			if (connection != null && connection.isConnected()) {
				connection.disconnect();
			}
			
			connection = null;
			
			if (isEmpty(config.getServiceName()) && isEmpty(config.getServer())) {
				return;
			}
			
			LOG.info("Connecting to " + config.getServer() + ":" + config.getPort());
			
			connect(config);
			if (connection == null) {
				this.connectionString = null;
			}
		}
	}
	
	@Override
	public boolean isConnected() {
		synchronized (lock) {
			return connection != null && connection.isConnected();
		}
	}
	
	public void disconnect() {
		synchronized (lock) {
			if (connection != null) {
				LOG.info("Disconnecting from XMPP server.");
				connection.disconnect();
				connection = null;
				connectionString = null;
			}
		}
	}
	
	public void sendMessage(String recipient, String message) {
		if (StringUtils.isBlank(message)) {
			return;
		}
		
		synchronized(lock) {
			if (connection == null) {
				return;
			}
			final ChatManager chatManager = connection.getChatManager();
			final Chat chat = chatManager.createChat(recipient, this);
			try {
				chat.sendMessage(message);
				LOG.debug(MessageFormat.format("Sent message to {0}: {1}", recipient, escape(message)));
			} catch (XMPPException e) {
				eventHandler.reportEvent(new ErrorEvent(this, "jabber.errors.send", new Object[] {recipient, e.getMessage()}, e));
			}
		}
	}
	
	public void processMessage(Chat chat, Message msg) {
		if (isBlank(msg.getBody())) {
			return;
		}
		
		LOG.debug(MessageFormat.format("Message ({2}) from {0}: {1}", chat.getParticipant(), msg.getBody(), msg.getType()));
		
		// TODO: don't raise event if msg type is error.  maybe raise some other event.
		synchronized(lock) {
			for (JabberChatListener l : listeners) {
				l.messageReceived(chat.getParticipant(), msg.getBody());
			}
		}
	}

	public void addMessageReceivedListener(JabberChatListener listener) {
		synchronized(lock) {
			listeners.add(listener);
		}
	}
	
	public EventHandler getEventHandler() {
		return eventHandler;
	}
	
	public void setEventHandler(EventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}
	
	void connect(JabberPluginConfig pluginConfig) {
		ConnectionConfiguration config;
		
		if (isBlank(pluginConfig.getServer())) {
			// Use DNS to query for host and port.
			config = new ConnectionConfiguration(pluginConfig.getServiceName());	
		} else {
			config = new ConnectionConfiguration(pluginConfig.getServer(), pluginConfig.getPort(), pluginConfig.getServiceName());
		}
		
		config.setSelfSignedCertificateEnabled(pluginConfig.isSelfSignedCertificateEnabled());
		config.setSASLAuthenticationEnabled(pluginConfig.isSASLAuthenticationEnabled());
		config.setSecurityMode(pluginConfig.getSecurityMode());
		
		connection = new XMPPConnection(config);
		
		final ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();
		
		try {
			// Set context class loader on current thread so threads started by Smack will inherit it.
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			
			connection.connect();
			String resource = pluginConfig.getResource();
			if (isBlank(resource)) {
				resource = "vulcan";
			}
			
			connection.login(pluginConfig.getUsername(), pluginConfig.getPassword(), resource);
			//thing(pluginConfig);
			
			connection.getChatManager().addChatListener(new ChatManagerListener() {
				public void chatCreated(Chat chat, boolean arg1) {
					chat.addMessageListener(XmppClient.this);
				}
			});
			LOG.info("Logged into " + pluginConfig.getServer() + ":" + pluginConfig.getPort() + " as " + pluginConfig.getUsername());
		} catch (XMPPException e) {
			eventHandler.reportEvent(new ErrorEvent(this, "jabber.errors.connect",
					new Object[] {config.getHost(), config.getPort(), pluginConfig.getUsername(), e.getMessage()}, e));
			
			connection = null;
		} finally {
			Thread.currentThread().setContextClassLoader(parentClassLoader);
		}
	}
	
	String escape(String string) {
		return StringEscapeUtils.escapeHtml(string);
	}

	void refreshConnection(String server, int port, String serviceName,
			String username, String password) {
		JabberPluginConfig config = new JabberPluginConfig();
		config.setServer(server);
		config.setPort(port);
		config.setServiceName(serviceName);
		config.setUsername(username);
		config.setPassword(password);
		refreshConnection(config);
	}
}
