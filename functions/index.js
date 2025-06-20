const { onCall } = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendNotification = onCall(async (request) => {
  const { token, title, body } = request.data;

  const message = {
    notification: { title, body },
    token,
  };

  try {
    const response = await admin.messaging().send(message);
    logger.info("Notification sent successfully:", response);
    return { success: true };
  } catch (error) {
    logger.error("Error sending notification:", error);
    return { success: false, error: error.message };
  }
});
