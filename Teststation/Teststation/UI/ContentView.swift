import SwiftUI
import CoreData

struct ContentView: View {
    @Environment(\.managedObjectContext) var managedObjectContext
    @FetchRequest(
        entity: Certificate.entity(),
        sortDescriptors: [NSSortDescriptor(keyPath: \Certificate.createts, ascending: false)]
        //,predicate: NSPredicate(format: "genre contains 'Action'")
    ) var certificates: FetchedResults<Certificate>
    
    @State var isPresented = false
    @State var certificatePhotos = [String : UIImage] ()
    
    var body: some View {
        NavigationView {
            List {
                ForEach(certificates) { cert in
                    NavigationLink( destination: CertificateView(certificate:cert) ) {
                        CertificateRow(certificate: cert, photo: self.certificatePhotos[cert.id!])
                    }
                    .listRowInsets(EdgeInsets(top: 10, leading: 10, bottom: 10, trailing: 16))
                }
                .onDelete(perform: deleteCertificate)
            }
            .listStyle(GroupedListStyle())
            .onAppear(perform: updatePictures)
            .sheet(isPresented: $isPresented) {
                AddCertificate { firstname, lastname, phonenumber, email, type, status, validto, photo in
                    addCertificate(
                        firstname: firstname,
                        lastname: lastname,
                        phonenumber: phonenumber,
                        email: email,
                        type: type,
                        status: status,
                        validto: validto,
                        photo: photo
                    )
                    isPresented = false
                }
            }
            .navigationTitle("certificatelist_title")
            .navigationBarItems(trailing: addButton())
        }
    }
    
    private func addButton() -> some View {
        return Button(action: { isPresented.toggle() }) {Image(systemName: "plus") }
    }
    
    private func deleteCertificate(at offsets: IndexSet) {
        offsets.forEach { index in
            let certificate = certificates[index]
            
            self.managedObjectContext.delete(certificate)
            
            let fetchRequest = NSFetchRequest<Picture>(entityName: "Picture")
            fetchRequest.predicate = NSPredicate(format: "id == %@", certificate.pictureid!)
            
            do {
                let pictures = try self.managedObjectContext.fetch(fetchRequest)
                
                print("deletePictures(\(certificate.pictureid!)): \(pictures.count)")
                for picture in pictures {
                    self.managedObjectContext.delete(picture)
                }
            }
            catch let error as NSError {
                print("could not fetch \(error), \(error.userInfo)")
            }
        }
        
        saveContext()
    }
    
    
    private func addCertificate(
        firstname: String,
        lastname: String,
        phonenumber: String,
        email: String,
        type: CertificateType,
        status: CertificateStatus,
        validto: Date,
        photo: UIImage) {
        
        let photoEntity = createPicture(image: photo, context: managedObjectContext)
        
        let cert = createCertificate(
            firstName: firstname,
            lastName: lastname,
            phoneNumber: phonenumber,
            email: email,
            validTo: validto,
            status: status,
            type: type,
            pictureid: photoEntity.id!,
            context:managedObjectContext
        )
        
        saveContext()
        
        certificatePhotos[cert.id!] = photo
    }
    
    
    private func saveContext() {
        do {
            try managedObjectContext.save()
        } catch {
            print("Error saving managed object context: \(error)")
        }
    }
    
    private func updatePictures() {
        (UIApplication.shared.delegate as!AppDelegate).persistentContainer.performBackgroundTask { context in
            let fetchRequest = NSFetchRequest<Picture>(entityName: "Picture")
            var newCertPhotos = [String: UIImage]()
            
            for c in certificates {
                if let picid = c.pictureid {
                    print("updatePictures(\(c.id!),pictid=\(picid))...")
                    
                    fetchRequest.predicate = NSPredicate(format: "id == %@", picid)
                    
                    do {
                        let pictures = try context.fetch(fetchRequest)
                        
                        print("updatePictures(\(picid)): \(pictures.count)")
                        for picture in pictures {
                            print("  \(picture.id!)")
                            print("  \(picture.format!)")
                            
                            newCertPhotos[c.id!] = UIImage(data: picture.data!)
                        }
                    }
                    catch let error as NSError {
                        print("could not fetch \(error), \(error.userInfo)")
                    }}
            }
            
            DispatchQueue.main.async {
                self.certificatePhotos = newCertPhotos
            }
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
