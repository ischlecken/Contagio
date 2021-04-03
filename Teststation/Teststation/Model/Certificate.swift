import Foundation
import CoreData

extension String {
    func localized(bundle: Bundle = .main, tableName: String = "Localizable") -> String {
        return NSLocalizedString(self, tableName: tableName, value: "**\(self)**", comment: "")
    }
}

enum CertificateStatus:Int8, CaseIterable {
    case unknown=0
    case negative=1
    case positive=2
}


enum CertificateType:Int8, CaseIterable {
    case rapidtest=0
    case pcrtest=1
    case vaccination=2
}

func createCertificate(
    firstName:String,
    lastName:String,
    phoneNumber:String,
    email:String,
    validTo:Date,
    status:CertificateStatus,
    type:CertificateType,
    context: NSManagedObjectContext) -> Certificate {
    let result = Certificate(context:context)
    
    result.createts = Date()
    result.validfrom = Date().advanced(by: 3600)
    result.validto = validTo
    result.id = UUID().uuidString
    result.phonenumber = phoneNumber
    result.firstname = firstName
    result.lastname = lastName
    result.email = email
    result.status = Int16(status.rawValue)
    result.type = Int16(type.rawValue)
    
    return result
}
