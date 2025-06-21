const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();
setGlobalOptions({ region: "europe-central2" });

exports.sendMessageNotification = onDocumentCreated("chats/{chatId}/messages/{messageId}", async (event) => {
  const message = event.data?.data();
  if (!message) return null;

  const receiverId = message.receiverId;
  const senderId = message.senderId;
  const text = message.messageText;

  const firestore = getFirestore();
  const userDoc = await firestore.collection("users").doc(receiverId).get();
  const fcmToken = userDoc.data()?.fcmToken;

  if (!fcmToken) {
    console.log("No FCM token for user", receiverId);
    return null;
  }

  const payload = {
    notification: {
      title: "New message",
      body: text.length > 100 ? text.substring(0, 97) + "..." : text,
    },
    data: {
      chatId: event.params.chatId,
      senderId,
    },
  };

  try {
    await getMessaging().sendToDevice(fcmToken, payload);
    console.log("Notification sent to", receiverId);
  } catch (error) {
    console.error("Error sending notification:", error);
  }

  return null;
});
