import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        Logger.shared.doInit()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}