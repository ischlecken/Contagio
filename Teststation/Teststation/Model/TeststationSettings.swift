import Foundation

final class TeststationSettings: ObservableObject {
    @Published var apibase: String {
        didSet {
            UserDefaults.standard.set(apibase, forKey: "apibase")
        }
    }
    
    init() {
        self.apibase = UserDefaults.standard.object(forKey: "apibase") as? String ?? "http://dvm1:13013"
    }
}
