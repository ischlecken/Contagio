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
    
    func getCertificate(id:String) -> Certificate? {
        var result:Certificate? = nil
        let fetchRequest = NSFetchRequest<Certificate>(entityName: "Certificate")
        fetchRequest.predicate = NSPredicate(format: "id == %@", id)
        
        do {
            let certs = try self.fetch(fetchRequest)
            
            print("getCertificate(\(id)): \(certs.count)")
            for cert in certs {
                result = cert
                break
            }
        }
        catch let error as NSError {
            print("could not fetch \(error), \(error.userInfo)")
        }
        
        return result
    }
    
    
    func cloneCertificate (certificate:Certificate) -> Certificate {
        let newCert = Certificate(context: self)
        newCert.createts = certificate.createts
        newCert.validfrom = certificate.validfrom
        newCert.validto = certificate.validto
        newCert.id = UUID().uuidString
        newCert.phonenumber = certificate.phonenumber
        newCert.firstname = certificate.firstname
        newCert.lastname = certificate.lastname
        newCert.email = certificate.email
        newCert.status = certificate.status
        newCert.type = certificate.type
        newCert.pictureid = certificate.pictureid
        newCert.issuestatus = certificate.issuestatus
        newCert.teststationid = certificate.teststationid
        newCert.testerid = certificate.testerid
        
        return newCert
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
        certificate.issuestatus = Int16(CertificateIssueStatus.created.rawValue)
        certificate.modifyts = Date()
        
        do {
            try self.save()
        } catch {
            print("Error saving managed object context: \(error)")
        }
    }
    
    func updateCertificateIssueStatus(certificate:Certificate,issueStatus:CertificateIssueStatus) {
        certificate.issuestatus = Int16(issueStatus.rawValue)
        certificate.modifyts = Date()
        
        do {
            try self.save()
        } catch {
            print("Error saving managed object context: \(error)")
        }
    }
}
