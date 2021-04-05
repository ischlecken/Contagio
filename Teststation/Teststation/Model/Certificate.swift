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
    case unknown=0
    case signed=1
    case refused=2
    case failed=3
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
        result.issuestatus = Int16(CertificateIssueStatus.unknown.rawValue)
        result.teststationid = "1"
        result.testerid = "0"
        
        return result
    }
    
    func addCertificate(
        firstname: String,
        lastname: String,
        phonenumber: String,
        email: String,
        type: CertificateType,
        status: CertificateStatus,
        validto: Date,
        photo: UIImage) -> Certificate {
        
        let photoEntity = self.createPicture(image: photo)
        let cert = self.createCertificate(
            firstName: firstname,
            lastName: lastname,
            phoneNumber: phonenumber,
            email: email,
            validTo: validto,
            status: status,
            type: type,
            pictureid: photoEntity.id!
        )
        
        do {
            try self.save()
        } catch {
            print("Error saving managed object context: \(error)")
        }
        
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
        
        do {
            try self.save()
        } catch {
            print("Error saving managed object context: \(error)")
        }
    }
    
    func updateCertificateStatus(certificate:Certificate,status:Int) {
        certificate.status = Int16(status)
        
        do {
            try self.save()
        } catch {
            print("Error saving managed object context: \(error)")
        }
    }
}
