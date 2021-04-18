import SwiftUI
import CoreData

struct CertificateList: View {
    @Environment(\.managedObjectContext) var managedObjectContext
    @EnvironmentObject var teststationState: TeststationState
    @FetchRequest(
        entity: Certificate.entity(),
        sortDescriptors: [
            NSSortDescriptor(keyPath: \Certificate.modifyts, ascending: false),
            NSSortDescriptor(keyPath: \Certificate.createts, ascending: false)
        ]
    ) var certificates: FetchedResults<Certificate>
    
    @State var isPresented = false
    @State var certificatePhotos = [String : UIImage] ()
    @State var showAlert = false
    @State var deletedOffsets:IndexSet? = nil
    
    var body: some View {
        NavigationView {
            List {
                Section(footer: HStack {
                            Text("Teststation: \(teststationState.teststation?.name ?? "-")")
                            Text("Tester: \(teststationState.tester?.person.lastName ?? "-")")}
                ) {
                    ForEach(certificates) { cert in
                        NavigationLink( destination: ModifyCertificate(
                            certificate: cert,
                            certifcatePhoto: certificatePhotos[cert.id!] ?? UIImage(named:"passdefaultimg")!,
                            selectedStatus: Int(cert.status) ) { mcr in
                            
                            if( mcr.shouldDelete ) {
                                managedObjectContext.deleteCertificate(certificate: mcr.cert)
                                managedObjectContext.saveContext()
                            }
                            else {
                                TeststationEngine.shared.startIssueOfCertificate(mcr: mcr) { issueStatus in
                                }
                            }
                        }) { CertificateRow(certificate: cert, photo: self.certificatePhotos[cert.id!]) }
                        .listRowInsets(EdgeInsets(top: 10, leading: 10, bottom: 10, trailing: 16))
                    }
                    .onDelete { offsets in
                        deletedOffsets = offsets
                        showAlert = true
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
                AddCertificate { acr in
                    let cert = managedObjectContext.addCertificate(acr: acr)
                    
                    managedObjectContext.saveContext()
                    
                    certificatePhotos[cert.id!] = acr.photo
                    isPresented = false
                    
                    TeststationEngine.shared.startIssueOfCertificate(
                        certificate:cert,
                        selectedStatus:cert.certStatus,
                        photo: acr.photo
                    ) { issueStatus in
                    }
                }
            }
            .alert(isPresented: $showAlert) {
                Alert(title:Text("alert_deletecertificate_title"),
                      message: Text("alert_deletecertificate_message"),
                      primaryButton: .destructive(Text("alert_deletecertificate_deletebutton"),action: deleteCertificates),
                      secondaryButton: .cancel()
                )
            }
            .navigationTitle("certificatelist_title")
            .navigationBarItems(
                trailing: Button(action: { isPresented.toggle() }) {Image(systemName: "plus") }
            )
        }
    }
    
    func deleteCertificates() {
        deletedOffsets?.forEach { index in
            let certificate = certificates[index]
            
            managedObjectContext.deleteCertificate(certificate: certificate)
        }
        managedObjectContext.saveContext()
        
        deletedOffsets = nil
        
    }
}

struct CertificateList_Previews: PreviewProvider {
    static var previews: some View {
        let context = (UIApplication.shared.delegate as!AppDelegate).persistentContainer.viewContext
        
        Group {
            CertificateList()
                .environment(\.managedObjectContext, context)
                .environmentObject(TeststationState().mockLogin())
        }
    }
}
