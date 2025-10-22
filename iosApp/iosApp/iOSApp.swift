import SwiftUI
import FirebaseCore
import FirebaseMessaging
import UserNotifications
import BackgroundTasks
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {

        // 🔥 Initialize Firebase
        FirebaseApp.configure()

        // 🔔 Setup notification delegates
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self

        // ✅ Register for remote notifications
        UIApplication.shared.registerForRemoteNotifications()

        // Register background tasks
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "com.uniguard.netguard_app.server_check", using: nil) { task in
            self.handleBackgroundTask(task)
        }

        print("✅ Firebase initialized")

        return true
    }

    // ✅ Called when FCM token is generated or refreshed
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let token = fcmToken else { return }
        print("📲 FCM Token: \(token)")
    }

    // ✅ Called when app receives push notification in FOREGROUND
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        print("🔔 Notification received in foreground: \(notification.request.content.userInfo)")

        // Tampilkan banner & sound meskipun app sedang dibuka
        completionHandler([.banner, .sound, .badge])
    }

    // ✅ Called when user taps on a notification (app in background or killed)
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        print("📩 Notification tapped: \(userInfo)")

        // Kamu bisa kirim event ke shared ViewModel (Compose) di sini kalau perlu
        completionHandler()
    }

    // ✅ Handle SILENT (data-only) notifications
    func application(
        _ application: UIApplication,
        didReceiveRemoteNotification userInfo: [AnyHashable : Any],
        fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void
    ) {
        print("📡 Received silent/data notification: \(userInfo)")

        // Ambil data dari payload FCM
        if let title = userInfo["title"] as? String,
           let body = userInfo["body"] as? String {
            // Menampilkan local notification manual
            showLocalNotification(title: title, body: body)
        }

        completionHandler(.newData)
    }

    // ✅ Helper function to show a manual (local) notification
    private func showLocalNotification(title: String, body: String) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default

        let request = UNNotificationRequest(
            identifier: UUID().uuidString,
            content: content,
            trigger: nil
        )

        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("❌ Failed to show local notification: \(error)")
            } else {
                print("✅ Local notification displayed: \(title)")
            }
        }
    }

    // ✅ Handle background tasks
    private func handleBackgroundTask(_ task: BGTask) {
        // For now, create a simple instance without Koin dependency injection
        // In a real app, you'd want to properly initialize Koin and get the instance
        let worker = ServerMonitoringWorker()
        worker.handleBackgroundTask(task: task)
    }
}



@main
struct iOSApp: App {
    init() {
        Logger.shared.doInit()
    }

    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
