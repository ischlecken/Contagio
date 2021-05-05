import UIKit
import CoreData


enum CertificateStatus:Int8, CaseIterable {
    case unknown=0
    case negative=1
    case positive=2
}


enum CertificateType:Int8, CaseIterable {
    case rapidtest=0
    case pcrtest=1
    case vaccination=2
    case unknown=42
}

enum CertificateIssueStatus:Int8, CaseIterable {
    case created=0
    case issued=1
    case refused=2
    case revoked=3
    case expired=4
    case unknown=42
    
    func isFinished() -> Bool {
        return self != CertificateIssueStatus.created
    }
}

extension Certificate {
    
    func fullName() -> String {
        return "\(firstname ?? "") \(lastname ?? "")"
    }
    
    func passURL() -> String {
        return "\(EnvConfig.contagioapiPassURL)/\(passid ?? "unknown")"
    }
    
    func updateStatus(status:CertificateStatus) {
        self.certStatus = status
        self.certIssueStatus = CertificateIssueStatus.created
        self.modifyts = Date()
    }
    
    func updateIssueStatus(issueStatus:CertificateIssueStatus) {
        self.certIssueStatus = issueStatus
        self.modifyts = Date()
    }
    
    var certIssueStatus: CertificateIssueStatus {
        set {
            self.issuestatus = Int16(newValue.rawValue)
        }
        get {
            CertificateIssueStatus(rawValue: Int8(self.issuestatus)) ?? .unknown
        }
    }
    
    var certType: CertificateType {
        set {
            self.type = Int16(newValue.rawValue)
        }
        get {
            CertificateType(rawValue: Int8(self.type)) ?? .unknown
        }
    }
    
    var certStatus: CertificateStatus {
        set {
            self.status = Int16(newValue.rawValue)
        }
        get {
            CertificateStatus(rawValue: Int8(self.status)) ?? .unknown
        }
    }
}

extension NSManagedObjectContext {
    
    func createCertificate(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        status: CertificateStatus,
        type: CertificateType,
        pictureid: String,
        teststationid: String,
        testerid: String) -> Certificate {
        
        let result = Certificate(context: self)
        
        result.createts = Date()
        result.id = UUID().uuidString
        result.phonenumber = phoneNumber
        result.firstname = firstName
        result.lastname = lastName
        result.email = email
        result.certStatus = status
        result.certType = type
        result.pictureid = pictureid
        result.certIssueStatus = CertificateIssueStatus.created
        result.teststationid = teststationid
        result.testerid = testerid
        
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
            status: acr.status,
            type: acr.type,
            pictureid: photoEntity.id!,
            teststationid: acr.teststationId,
            testerid: acr.testerId
        )
        
        return cert
    }
    
    func deleteCertificate(certificate: Certificate) {
        self.delete(certificate)
        
        if let pictureid = certificate.pictureid {
            let fetchRequest = NSFetchRequest<Picture>(entityName: "Picture")
            fetchRequest.predicate = NSPredicate(format: "id == %@", pictureid)
            
            do {
                let pictures = try self.fetch(fetchRequest)
                
                print("deletePictures(\(pictureid)): \(pictures.count)")
                for picture in pictures {
                    self.delete(picture)
                }
            }
            catch let error as NSError {
                print("could not fetch \(error), \(error.userInfo)")
            }
        }
    }
}
