import Foundation
import CoreData

func createTestCertificate(firstName:String, lastName:String, phoneNumber:String,context: NSManagedObjectContext) -> TestCertificate {
    let result = TestCertificate(context:context)
    
    result.createts = Date()
    result.validuntil = Date().advanced(by: 86400)
    result.id = UUID().uuidString
    result.phonenumber = phoneNumber
    result.firstname = firstName
    result.lastname = lastName
    result.teststatus = 0
    
    return result
}
