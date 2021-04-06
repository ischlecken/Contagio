import SwiftUI
import CoreData

struct ContentView: View {
    @Environment(\.managedObjectContext) var managedObjectContext
    @FetchRequest(
        entity: Certificate.entity(),
        sortDescriptors: [NSSortDescriptor(keyPath: \Certificate.createts, ascending: false)]
    ) var certificates: FetchedResults<Certificate>
    
    @State var issueCertificate = false
    @State var isPresented = false
    @State var certificatePhotos = [String : UIImage] ()
    
    var body: some View {
        NavigationView {
            List {
                if( issueCertificate ) {
                    ProgressView("Issue Certificate")
                }
                ForEach(certificates) { cert in
                    NavigationLink( destination: ModifyCertificate(
                        certificate:cert,
                        certifcatePhoto: certificatePhotos[cert.id!] ?? UIImage(named:"passdefaultimg")!,
                        selectedStatus: Int(cert.status) ) { cert,selectedStatus,shouldDelete in
                        
                        if( shouldDelete) {
                            managedObjectContext.deleteCertificate(certificate: cert)
                        }
                        else {
                            managedObjectContext.updateCertificateStatus(certificate: cert, status: selectedStatus)
                            
                            issueCertificate = true
                            (UIApplication.shared.delegate as!AppDelegate).teststationEngine.startIssueOfCertificate(certificate: cert) { cid,cissuestatus in
                                print("startIssueOfCertificate:\(cid) \(cissuestatus)")
                                
                                if( cissuestatus != CertificateIssueStatus.created && cissuestatus != CertificateIssueStatus.pending) {
                                    issueCertificate = false
                                }
                            }
                        }
                        
                    }) { CertificateRow(certificate: cert, photo: self.certificatePhotos[cert.id!]) }
                    .listRowInsets(EdgeInsets(top: 10, leading: 10, bottom: 10, trailing: 16))
                }
                .onDelete { offsets in
                    offsets.forEach { index in
                        let certificate = certificates[index]
                        
                        managedObjectContext.deleteCertificate(certificate: certificate)
                    }
                }
            }
            .listStyle(GroupedListStyle())
            .onAppear {
                (UIApplication.shared.delegate as!AppDelegate).persistentContainer.performBackgroundTask { context in
                    let newCertPhotos = context.loadPictures(certificates: certificates)
                    
                    DispatchQueue.main.async {
                        self.certificatePhotos = newCertPhotos
                    }
                }
            }
            .sheet(isPresented: $isPresented) {
                AddCertificate { firstname, lastname, phonenumber, email, type, status, validto, photo in
                    let cert = managedObjectContext.addCertificate(
                        firstname: firstname,
                        lastname: lastname,
                        phonenumber: phonenumber,
                        email: email,
                        type: type,
                        status: status,
                        validto: validto,
                        photo: photo)
                    
                    certificatePhotos[cert.id!] = photo
                    isPresented = false
                    issueCertificate = true
                    (UIApplication.shared.delegate as!AppDelegate).teststationEngine.startIssueOfCertificate(certificate: cert) { cid,cissuestatus in
                        print("startIssueOfCertificate:\(cid) \(cissuestatus)")
                        
                        if( cissuestatus != CertificateIssueStatus.created && cissuestatus != CertificateIssueStatus.pending) {
                            issueCertificate = false
                        }
                    }
                }
            }
            .navigationTitle("certificatelist_title")
            .navigationBarItems(
                trailing: Button(action: { isPresented.toggle() }) {Image(systemName: "plus") }
            )
        }
    }
    
}

struct ContentView_Previews: PreviewProvider {
    
    static var previews: some View {
        let context = (UIApplication.shared.delegate as!AppDelegate).persistentContainer.viewContext
        
        Group {
            ContentView().environment(\.managedObjectContext, context)
        }
    }
}
