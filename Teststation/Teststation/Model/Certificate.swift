import Foundation
import CoreData

extension String {
    func localized(bundle: Bundle = .main, tableName: String = "Localizable") -> String {
        return NSLocalizedString(self, tableName: tableName, value: "**\(self)**", comment: "")
    }
}

enum CertificateStatus:Int8 {
    case unknown=0
    case negative=1
    case positive=2
}


enum CertificateType:Int8 {
    case unknown=0
    case rapidtest=1
    case pcrtest=2
    case vaccination=3
}

func createCertificate(firstName:String, lastName:String, phoneNumber:String,context: NSManagedObjectContext) -> Certificate {
    let result = Certificate(context:context)
    
    result.createts = Date()
    result.validfrom = Date().advanced(by: 3600)
    result.validto = Date().advanced(by: 86400)
    result.id = UUID().uuidString
    result.phonenumber = phoneNumber
    result.firstname = firstName
    result.lastname = lastName
    result.status = Int16(CertificateStatus.unknown.rawValue)
    result.type = Int16(CertificateType.rapidtest.rawValue)
    
    return result
}
