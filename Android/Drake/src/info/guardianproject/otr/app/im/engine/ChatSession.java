/*
 * Copyright (C) 2007 Esmertec AG.
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.guardianproject.otr.app.im.engine;

import info.guardianproject.otr.OtrChatManager;

import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.ImEntity;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Message;

import android.util.Log;

/**
 * A ChatSession represents a conversation between two users. A ChatSession has
 * a unique participant which is either another user or a group.
 */
public class ChatSession
{
    private ImEntity mParticipant;
    private ChatSessionManager mManager;
    
    private OtrChatManager mOtrChatManager;
    
    private CopyOnWriteArrayList<MessageListener> mListeners;
    private Vector<Message> mHistoryMessages;

    /**
     * Creates a new ChatSession with a particular participant.
     *
     * @param participant the participant with who the user communicates.
     * @param connection the underlying network connection.
     */
    ChatSession(ImEntity participant, ChatSessionManager manager)
    {
        mParticipant = participant;
        mManager = manager;
        mListeners = new CopyOnWriteArrayList<MessageListener>();
        mHistoryMessages = new Vector<Message>();
    }

    public ImEntity getParticipant()
    {
        return mParticipant;
    }

    public void setParticipant(ImEntity participant)
    {
        mParticipant = participant;
    }

    public boolean isGroupSession()
    {
    	boolean result = false;
    	if (mParticipant != null)
    	{
    		if (mParticipant instanceof ChatGroup)
    		{
    			result = true;
    		}
    		else if (mParticipant instanceof Contact)
    		{
    			Contact contact = (Contact)mParticipant;
    			if (contact.getType() == Contact.Type.GROUP)
    			{
    				result = true;
    			}
    		}
    	}
    	return result;
    }
    
    public void setOtrChatManager(OtrChatManager otrChatManager)
    {
        mOtrChatManager = otrChatManager;
    }
    
    public OtrChatManager getOtrChatManager()
    {
    	return mOtrChatManager;
    }
    /**
     * Adds a MessageListener so that it can be notified of any new message in
     * this session.
     *
     * @param listener
     */
    public synchronized void addMessageListener(MessageListener listener)
    {
    	if ((listener != null) && !mListeners.contains(listener))
    	{
            mListeners.add(listener);
        }
    }

    /**
     * Removes a listener from this session.
     *
     * @param listener
     */
    public synchronized void removeMessageListener(MessageListener listener)
    {
        mListeners.remove(listener);
    }

    /**
     * Sends a text message to other participant(s) in this session
     * asynchronously and adds the message to the history.
     * TODO: more docs on async callbacks.
     *
     * @param text the text to send.
     */
    // TODO these sendMessageAsync() should probably be renamed to sendMessageAsyncAndLog()/
    /*
    public void sendMessageAsync(String text) {
        Message message = new Message(text);
        sendMessageAsync(message);
    }*/

    /**
     * Sends a message to other participant(s) in this session asynchronously 
     * and adds the message to the history.
     * TODO: more docs on async callbacks.
     *
     * @param message the message to send.
     */
    public void sendMessageAsync(Message message)
    {
    	if (message.getTo() == null)
    	{
    		message.setTo(mParticipant.getAddress());
    	}
        
        // TODO OTRCHAT setFrom here, therefore add the mConnection in ChatSession
        mHistoryMessages.add(message);
        
        String localUserId = message.getFrom();
        String remoteUserId =  message.getTo();
        
        if (mOtrChatManager != null)
        {
        	String transformBody = mOtrChatManager.transformSending(localUserId, remoteUserId, message.getBody());
        	message.setBody(transformBody);
        }
        mManager.sendMessageAsync(this, message);
    }

    /**
     * Called by ChatSessionManager when received a message of the ChatSession.
     * All the listeners registered in this session will be notified.
     *
     * @param message the received message.
     */
    public void onReceiveMessage(Message message)
    {
        mHistoryMessages.add(message);
        //  BUG it only seems to find the most recently added listener.ve
        for (MessageListener listener : mListeners)
        {
            listener.onIncomingMessage(this, message);
        }
    }
    
    public void onMessageReceipt(String id)
    {
        for (MessageListener listener : mListeners)
        {
            listener.onIncomingReceipt(this, id);
        }
    }
    
    public void onReceiptsExpected()
    {
        for (MessageListener listener : mListeners)
        {
            listener.onReceiptsExpected(this);
        }
    }

    /**
     * Called by ChatSessionManager when an error occurs to send a message.
     * @param message
     *
     * @param error the error information.
     */
    public void onSendMessageError(Message message, ImErrorInfo error)
    {
        for (MessageListener listener : mListeners)
        {
            listener.onSendMessageError(this, message, error);
        }
    }

    public void onSendMessageError(String messageId, ImErrorInfo error)
    {
        for(Message message : mHistoryMessages)
        {
            if(messageId.equals(message.getID()))
            {
                onSendMessageError(message, error);
                return;
            }
        }
        Log.i("ChatSession", "Message has been removed when we get delivery error:" + error);
    }
    
    /**
     * Returns a unmodifiable list of the history messages in this session.
     *
     * @return a unmodifiable list of the history messages in this session.
     */
    public List<Message> getHistoryMessages()
    {
        return Collections.unmodifiableList(mHistoryMessages);
    }
}
