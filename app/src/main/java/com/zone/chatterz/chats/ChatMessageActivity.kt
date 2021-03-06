package com.zone.chatterz.chats

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.mikhaellopez.circularimageview.CircularImageView
import com.zone.chatterz.adapter.ChatsAdapter
import com.zone.chatterz.connection.Connection
import com.zone.chatterz.connection.FirebaseMethods
import com.zone.chatterz.connection.RequestCallback
import com.zone.chatterz.inferfaces.APIService
import com.zone.chatterz.model.Chat
import com.zone.chatterz.model.User
import com.zone.chatterz.notification.*
import com.zone.chatterz.R
import retrofit2.Call
import java.io.File

class ChatMessageActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mChat: MutableList<Chat>
    private lateinit var userId: String
    private lateinit var chatsRecyclerview : RecyclerView
    private lateinit var sendMessageButton : ImageView
    private lateinit var backArrow : ImageView
    private lateinit var editextMessage : EditText
    private lateinit var userNameChatBox : TextView
    private lateinit var lastOnline : TextView
    private lateinit var profileImg_chatBar : CircularImageView
    private lateinit var apiService: APIService

    private var isActive: String = "active"
    private var notify = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatmessage)

        //getIntent to getUserid of userchats
        val intent = intent
        val id: String = intent.getStringExtra("UserId")
        userId = id

        userNameChatBox = findViewById(R.id.userNameChatBox)
        sendMessageButton = findViewById(R.id.sendMessageButton)
        backArrow = findViewById(R.id.backArrow)
        editextMessage = findViewById(R.id.editextMessage)
        chatsRecyclerview = findViewById(R.id.chatsRecyclerview)
        lastOnline = findViewById(R.id.lastOnline)
        profileImg_chatBar = findViewById(R.id.profileImg_chatBar)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        apiService = Client.getClient()?.create(APIService::class.java)!!

        //RecyclerView for Chats setting linearlayoutManager
        chatsRecyclerview.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        chatsRecyclerview.layoutManager = linearLayoutManager

        // databaseReference set the adapter of recycler view of chats
        readAllChats()

        //OnClick of send Message button calls the method sendMessage()
        //sendMessage() sends the load the message on data with sender and receiver info
        sendMessageButton.setOnClickListener {
            notify = true
            val message = editextMessage.text.toString()
            if (!message.equals("")) {
                val sender = firebaseUser.uid
                val reciever = id
                sendMessage(message, sender, reciever)
            } else {
                Toast.makeText(this, "Can't send empty Message", Toast.LENGTH_SHORT).show()
            }
        }

        //set backArrow of ChatMessage Screen
        backArrow.setOnClickListener(this)

        seenMessage(id)
    }

    override fun onStart() {
        super.onStart()
        isActive = "active"
    }

    override fun onStop() {
        super.onStop()
        isActive = "inactive"
    }

    override fun onBackPressed() {
        finish()
    }

    private fun readAllChats() {
        FirebaseMethods.singleValueEvent(Connection.userRef+ File.separator+userId, object : RequestCallback() {
            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                setProfileNameAndImgAppBar(dataSnapshot)
                readMessage()
            }
        })
    }

    private fun sendMessage(message: String, sender: String, reciever: String) {
        databaseReference = FirebaseDatabase.getInstance().reference
        val hashMap = hashMapOf<String, Any>()
        hashMap.put("message", message)
        hashMap.put("sender", sender)
        hashMap.put("receiver", reciever)
        hashMap.put("isSeen", false)

        //Clear Edittext and make ready to take next chat
        editextMessage.setText("")
        databaseReference.child("Chats").child(sender).child(reciever).push().setValue(hashMap)
        databaseReference.child("Chats").child(reciever).child(sender).push().setValue(hashMap)

        val mes = message
        FirebaseMethods.singleValueEvent(Connection.userRef+File.separator+userId, object : RequestCallback() {
            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    if (notify) {
                        sendNotification(mes, reciever, user.username,user.imageUrl)
                    }
                    notify = false
                }
            }
        })
    }

    private fun sendNotification(mes: String, reciever: String, username: String, imageUrl: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Tokens")
        val query = databaseReference.orderByKey().equalTo(reciever)
        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (data in p0.children) {
                    val token = data.getValue(Token::class.java)
                    if (token != null) {
                        val data = Data(Connection.user,imageUrl,mes,NotificationType.chat,userId)
                        val sender = Sender(data, token.token)
                        apiService.sendNotification(sender)
                            .enqueue(object : retrofit2.Callback<Response> {
                                override fun onFailure(call: Call<Response>, t: Throwable) {
                                }

                                override fun onResponse(
                                    call: Call<Response>,
                                    response: retrofit2.Response<Response>
                                ) {
                                    if (response.code() == 200) {
                                        if (response.body()?.sucess != 1) {
                                            Toast.makeText(
                                                this@ChatMessageActivity,
                                                "Failed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            })
                    }
                }
            }
        })
    }

    private fun readMessage() {

        mChat = mutableListOf()
        val ref = Connection.userChats+File.separator+Connection.user+File.separator+userId
        FirebaseMethods.addValueEvent(ref, object : RequestCallback() {
            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                mChat.clear()
                for (dataset in dataSnapshot.children) {
                    val chat = dataset.getValue(Chat::class.java)
                    if (chat != null) {
                        mChat.add(chat)
                    }
                }
                val chatsAdapter = ChatsAdapter(this@ChatMessageActivity, mChat)
                chatsRecyclerview.adapter = chatsAdapter
            }
        })
    }

    private fun setProfileNameAndImgAppBar(dataSnapshot: DataSnapshot) {
        //Set the Username and Profile image of the user on the appbar of the chatMessage Activity
        //It also shows the status of the user whom the cureent user is want to send message is Online/Offline as status
        val user = dataSnapshot.getValue(User::class.java)
        if (user != null) {
            if (user.imageUrl.equals("null")) {
                profileImg_chatBar.setImageResource(R.drawable.google_logo)
            } else {
                Glide.with(this).load(user.imageUrl).into(profileImg_chatBar)
            }
            userNameChatBox.text = user.username
            lastOnline.text = user.status
        }
    }

    private fun seenMessage(userId: String) {
        FirebaseMethods.addValueEventChild(Connection.userChats, object : RequestCallback() {
            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    val chat = data.getValue(Chat::class.java)
                    if (chat != null && (chat.receiver.equals(firebaseUser.uid) && chat.sender.equals(
                            userId
                        )) && !chat.isSeen
                    ) {
                        if (isActive.equals("active")) {
                            val hashMap = HashMap<String, Any>()
                            hashMap.put("isSeen", true)
                            data.ref.updateChildren(hashMap)
                        }
                    }

                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            backArrow -> {
                onBackPressed()
            }
            else -> {
                return
            }
        }
    }
}