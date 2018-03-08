/*
 * Copyright (C) 2015 Mr.Simple <bboyfeiyu@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enigma.message.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.enigma.message.dynamic.util.Log;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 事件处理在UI线程,通过Handler将事件处理post到UI线程的消息队列
 *
 */
public class UIThreadEventHandler implements EventHandler {

    private static final int MAX_HANDLE_MESSAGE_TIME = 10;

    private Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

    private boolean handlerActive = false;

    /**
     * ui handler
     */
    private Handler mUIHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            boolean rescheduled = false;
            try {
                long started = SystemClock.uptimeMillis();
                while (true) {
                    Runnable pendingPost = queue.poll();
                    if (pendingPost == null) {
                        synchronized (UIThreadEventHandler.this) {
                            handlerActive = false;
                            return;
                        }
                    }
                    pendingPost.run();
                    long timeInMethod = SystemClock.uptimeMillis() - started;
                    if (timeInMethod >= MAX_HANDLE_MESSAGE_TIME) {
                        if (!sendMessageAtFrontOfQueue(obtainMessage())) {
                            Log.error("UIThreadEventHandler","can not send message");
                        }
                        rescheduled = true;
                        return;
                    }
                }
            } finally {
                synchronized (UIThreadEventHandler.this){
                    handlerActive = rescheduled;
                }
            }
        }
    };

    public void handleEvent(Runnable runnable) {
        if(Looper.getMainLooper() == Looper.myLooper()){
            runnable.run();
        } else {
            queue.offer(runnable);
            synchronized (this) {
                if (!handlerActive) {
                    handlerActive = true;
                    if (!mUIHandler.sendMessageAtFrontOfQueue(mUIHandler.obtainMessage())) {
                        Log.error("UIThreadEventHandler","can not send message");
                    }
                }
            }
        }
    }

}
