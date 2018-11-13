const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
exports.approveTimesheet = functions.https.onRequest((req, res) => {
  var db = admin.database();
  var userId = req.query.userId;
  var company = req.query.company;
  var timesheetId = req.query.timesheetId;
  var ref = db.ref("/users/" + userId + "/timesheets/" + company + "/" + timesheetId);
  ref.child("approved").set("approved");
  console.log("Timesheet LQ0fmyiCbqXO7CSmL9x approved");
  res.send("Timesheet Approved");
});

exports.rejectTimesheet = functions.https.onRequest((req, res) => {
     var db = admin.database();
     var userId = req.query.userId;
     var company = req.query.company;
     var timesheetId = req.query.timesheetId;
     var ref = db.ref("/users/" + userId + "/timesheets/" + company + "/" + timesheetId);
     ref.child("approved").set("rejected");
     console.log("Timesheet LQ0fmyiCbqXO7CSmL9x rejected");
     res.send("Timesheet Rejected");
   });