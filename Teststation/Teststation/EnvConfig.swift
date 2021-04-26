import Foundation

public enum EnvConfig {
    // MARK: - Keys
    enum Keys {
        enum Plist {
            static let baseUrl = "CONTAGIOAPI_BASEURL"
            static let userName = "CONTAGIOAPI_USERNAME"
            static let password = "CONTAGIOAPI_PASSWORD"
        }
    }
    
    // MARK: - Plist
    private static let infoDictionary: [String: Any] = {
        guard let dict = Bundle.main.infoDictionary else {
            fatalError("Plist file not found")
        }
        return dict
    }()
    
    // MARK: - Plist values
    static let contagioapiBaseURL: String = {
        guard let rootURLstring = EnvConfig.infoDictionary[Keys.Plist.baseUrl] as? String else {
            fatalError("baseUrl not set in plist for this environment")
        }
        return rootURLstring
    }()
    
    static let contagioapiUsername: String = {
        guard let userName = EnvConfig.infoDictionary[Keys.Plist.userName] as? String else {
            fatalError("userName not set in plist for this environment")
        }
        return userName
    }()
    
    
    static let contagioapiPassword: String = {
        guard let password = EnvConfig.infoDictionary[Keys.Plist.password] as? String else {
            fatalError("password not set in plist for this environment")
        }
        return password
    }()
}
