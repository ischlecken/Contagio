import UIKit
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

enum CertificateIssueStatus:Int8, CaseIterable {
    case created=0
    case pending=1
    case signed=2
    case refused=3
    case failed=4
    
    func isFinished() -> Bool {
        return self == CertificateIssueStatus.signed || self == CertificateIssueStatus.refused || self == CertificateIssueStatus.failed
    }
}

extension Certificate {
    func updateStatus(status:CertificateStatus) {
        self.status = Int16(status.rawValue)
        self.issuestatus = Int16(CertificateIssueStatus.created.rawValue)
        self.modifyts = Date()
    }
    
    func updateIssueStatus(issueStatus:CertificateIssueStatus) {
        self.issuestatus = Int16(issueStatus.rawValue)
        self.modifyts = Date()
    
    }
}

extension NSManagedObjectContext {
    
    func createCertificate(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        validTo: Date,
        status: CertificateStatus,
        type: CertificateType,
        pictureid: String) -> Certificate {
        
        let result = Certificate(context: self)
        
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
        result.pictureid = pictureid
        result.issuestatus = Int16(CertificateIssueStatus.created.rawValue)
        result.teststationid = "1"
        result.testerid = "0"
        
        return result
    }
    
    func getCertificate (objectID:NSManagedObjectID) throws -> Certificate? {
        let result = try self.existingObject(with: objectID)
        
        return result as? Certificate
    }
    
    func addCertificate(acr:AddCertificateResponse) -> Certificate {
        
        let photoEntity = self.createPicture(image: acr.photo)
        
        let cert = self.createCertificate(
            firstName: acr.firstname,
            lastName: acr.lastname,
            phoneNumber: acr.phonenumber,
            email: acr.email,
            validTo: acr.validto,
            status: acr.status,
            type: acr.type,
            pictureid: photoEntity.id!
        )
        
        return cert
    }
    
    func deleteCertificate(certificate: Certificate) {
        self.delete(certificate)
        
        let fetchRequest = NSFetchRequest<Picture>(entityName: "Picture")
        fetchRequest.predicate = NSPredicate(format: "id == %@", certificate.pictureid!)
        
        do {
            let pictures = try self.fetch(fetchRequest)
            
            print("deletePictures(\(certificate.pictureid!)): \(pictures.count)")
            for picture in pictures {
                self.delete(picture)
            }
        }
        catch let error as NSError {
            print("could not fetch \(error), \(error.userInfo)")
        }
        
    }
}
