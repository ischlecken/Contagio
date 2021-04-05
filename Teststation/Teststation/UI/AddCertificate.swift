import SwiftUI

struct AddCertificate: View {
    @State var firstname = ""
    @State var lastname = ""
    @State var phonenumber = ""
    @State var email = ""
    @State var validto = Date().advanced(by: 86400)
    
    @State var selectedType = 0
    @State var selectedStatus = 0
    
    @State var showingImagePicker = false
    @State var certifcatePhoto: UIImage?
    @State var image: Image = Image(uiImage: UIImage(named: "passdefaultimg")!)
    
    let types:[Int8] = CertificateType.allCases.map{ $0.rawValue }
    let status:[Int8] = CertificateStatus.allCases.map{ $0.rawValue }
    
    var isValid: Bool {
        firstname.count > 2 && lastname.count > 3 && phonenumber.count > 5 && certifcatePhoto != nil
    }
    
    let onComplete: (String, String, String, String, CertificateType, CertificateStatus, Date, UIImage) -> Void
    
    var body: some View {
        NavigationView {
            Form {
                Section() {
                    VStack(alignment: .leading) {
                        Text("addcert_firstname").font(.caption)
                        TextField("addcert_firstname_placeholder", text: $firstname)
                    }
                    VStack(alignment: .leading) {
                        Text("addcert_lastname").font(.caption)
                        TextField("addcert_lastname_placeholder", text: $lastname)
                    }
                    HStack {
                        Text("addcert_addphoto").font(.caption)
                        Spacer()
                        Button(action:showImagePicker) {
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .frame(height: 120, alignment: .top)
                        }}
                }
                Section() {
                    VStack(alignment: .leading) {
                        Text("addcert_phonenumber").font(.caption)
                        TextField("addcert_phonenumber_placeholder", text: $phonenumber).keyboardType(.numberPad)
                    }
                    VStack(alignment: .leading) {
                        Text("addcert_email").font(.caption)
                        TextField("addcert_email_placeholder", text: $email).keyboardType(.emailAddress)
                    }
                }
                Section()  {
                    VStack(alignment: .leading) {
                        Text("addcert_type").font(.caption)
                        Picker(selection: $selectedType, label: Text("Certification Type")) {
                            ForEach(types.indices) { i in
                                Text("certificatetype_\(types[i])".localized()).tag(i)
                            }
                        }.pickerStyle(SegmentedPickerStyle())
                    }
                    VStack(alignment: .leading) {
                        Text("addcert_status").font(.caption)
                        Picker(selection: $selectedStatus, label: Text("Certification Status")) {
                            ForEach(status.indices) { i in
                                Text("certificatestatus_\(status[i])".localized()).tag(i)
                            }
                        }.pickerStyle(SegmentedPickerStyle())
                    }
                }
                Section {
                    DatePicker(selection: $validto,displayedComponents: [.hourAndMinute, .date]) {
                        Text("addcert_validto").foregroundColor(Color(.gray))
                    }
                }
                Section {
                    Button(action: addCertificateAction) {
                        Text("addcert_addbutton")
                    }
                    .disabled(!isValid)
                }
            }
            .navigationBarTitle(Text("addcert_title"), displayMode: .inline)
            .sheet(isPresented: $showingImagePicker, onDismiss: loadImage) {
                ImagePicker(image: self.$certifcatePhoto)
            }
        }
    }
    
    private func showImagePicker() {
        print("showImagePicker")
        
        self.showingImagePicker = true
    }
    
    private func loadImage() {
        var img = UIImage(named: "passdefaultimg")!
        
        if let photo = self.certifcatePhoto {
            img = photo.resizeImage(300, opaque: true)
        }
        
        print("loadImage() size=\(img.size)")
        
        self.image = Image(uiImage: img)
    }
    
    private func addCertificateAction() {
        onComplete(
            firstname,
            lastname,
            phonenumber,
            email,
            CertificateType(rawValue:types[selectedType])!,
            CertificateStatus(rawValue:status[selectedStatus])!,
            validto,
            certifcatePhoto!)
    }
}


struct AddCertificate_Previews: PreviewProvider {
    
    static var previews: some View {
        AddCertificate{ firstname, lastname, phonenumber, email, type, status, validto, photo in
        }
    }
}
