import SwiftUI
import Combine
import CoreData

class TeststationEngine {
    
    static let shared = TeststationEngine()
    
    var contagioAPISubscription : AnyCancellable?
    var refreshSubscriptions = Set<AnyCancellable>()
    var passInfo: [PassInfo]?
    var error: TeststationError?
    
    init() {
        print("TeststationEngine()")
    }
    
    func refreshCertificateStatus() {
        print("refreshCertificateStatus()")
        
        refreshSubscriptions.removeAll()
        
        let persistentContainer = (UIApplication.shared.delegate as!AppDelegate).persistentContainer
        
        persistentContainer.performBackgroundTask{ context in
            print("update certificate status()")
            
            let pendingCerts = context.getPendingCertificateSerialnumbers()
            
            pendingCerts.forEach { cert in
                
                print("  pendingCert=\(cert.serialnumber!)")
                try? ContagioAPI
                    .getPassInfo(serialNumber: cert.serialnumber!)
                    .sink(
                        receiveCompletion: {completion in
                            switch completion {
                            case .finished:
                                break
                            case .failure(let error):
                                switch error {
                                case .notFound:
                                    cert.updateIssueStatus(issueStatus: .deleted)
                                    context.saveContext()
                                    break
                                default:
                                    break
                                }
                            }
                        },
                        receiveValue: { passInfo in
                            print("passInfo=\(passInfo)")
                            
                            cert.updateIssueStatus(issueStatus: passInfo.issueStatus.toCertificateIssueStatus())
                            cert.validuntil = passInfo.validUntil
                            context.saveContext()
                        })
                    .store(in: &self.refreshSubscriptions)
            }
        }
    }
    
    func startIssueOfCertificate(mcr: ModifyCertificateResponse,
                                 onChange: @escaping (CertificateIssueStatus)->Void) {
        
        let persistentContainer = (UIApplication.shared.delegate as!AppDelegate).persistentContainer
        
        mcr.cert.updateIssueStatus(issueStatus:.created)
        mcr.cert.managedObjectContext?.saveContext()
        
        let updatePassRequest = UpdatePassRequest(
            serialNumber: mcr.cert.serialnumber!,
            testResult: TestResultType.fromCertificateStatus(status: mcr.selectedStatus),
            validUntil: mcr.validuntil
        )
        
        contagioAPISubscription = try? ContagioAPI
            .updatePassInfo(updatePassRequest: updatePassRequest)
            .sink(
                receiveCompletion: { [unowned self] completion in
                    switch completion {
                    case .finished:
                        break
                    case .failure(let error):
                        print("Error: \(error)")
                        self.error = error
                        
                        let context = persistentContainer.newBackgroundContext()
                        guard let cert = try? context.getCertificate(objectID: mcr.cert.objectID) else {
                            return
                        }
                        
                        cert.updateIssueStatus(issueStatus: .rejected)
                        context.saveContext()
                    }
                    
                    contagioAPISubscription = nil
                },
                receiveValue: { result in
                    let context = persistentContainer.newBackgroundContext()
                    guard let cert = try? context.getCertificate(objectID: mcr.cert.objectID) else {
                        return
                    }
                    
                    print("result=\(result)")
                    
                    sleep(4)
                    
                    cert.validuntil = result.validUntil
                    cert.passid = result.passId
                    cert.modifyts = result.updated
                    cert.updateIssueStatus(issueStatus: result.issueStatus.toCertificateIssueStatus())
                    context.saveContext()
                }
            )
    }
    
    func startIssueOfCertificate(certificate: Certificate,
                                 selectedStatus:CertificateStatus,
                                 photo: UIImage,
                                 onChange: @escaping (CertificateIssueStatus)->Void) {
        
        let persistentContainer = (UIApplication.shared.delegate as!AppDelegate).persistentContainer
        
        certificate.updateIssueStatus(issueStatus:.created)
        certificate.managedObjectContext?.saveContext()
        
        let createPassRequest = CreatePassRequest(
            firstName: certificate.firstname!,
            lastName: certificate.lastname!,
            phoneNo: certificate.phonenumber!,
            email: certificate.email,
            teststationId: certificate.teststationid!,
            testerId: certificate.testerid!,
            testResult: TestResultType.fromCertificateStatus(status: selectedStatus),
            testType: TestType.fromCertificateType(type: certificate.certType),
            photo: photo.pngData()!
        )
        
        contagioAPISubscription = try? ContagioAPI
            .createPassInfo(createPassRequest: createPassRequest)
            .sink(
                receiveCompletion: { [unowned self] completion in
                    switch completion {
                    case .finished:
                        break
                    case .failure(let error):
                        print("Error: \(error)")
                        self.error = error
                        
                        let context = persistentContainer.newBackgroundContext()
                        guard let cert = try? context.getCertificate(objectID: certificate.objectID) else {
                            return
                        }
                        
                        cert.updateIssueStatus(issueStatus: .rejected)
                        context.saveContext()
                    }
                    
                    contagioAPISubscription = nil
                },
                receiveValue: { result in
                    let context = persistentContainer.newBackgroundContext()
                    guard let cert = try? context.getCertificate(objectID: certificate.objectID) else {
                        return
                    }
                    
                    print("result=\(result)")
                    
                    cert.serialnumber = result.serialNumber
                    cert.updateIssueStatus(issueStatus: CertificateIssueStatus.created)
                    context.saveContext()
                }
            )
    }
    
}
