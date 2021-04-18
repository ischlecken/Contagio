import SwiftUI
import Combine

class PassLoader: ObservableObject {
    @Published var pass: Data? = nil
    @Published var isLoading: Bool = false
    @Published var showPass: Bool = false
    
    var loadPassSubscription: AnyCancellable? = nil
    
    func loadingPass(passid: String) {
        isLoading = false
        
        if( pass != nil ) {
            showPass = true
        
            return
        }
        
        if( loadPassSubscription == nil ) {
            isLoading = true
            
            loadPassSubscription = try? ContagioAPI
                .getPass(passId: passid)
                .sink(
                    receiveCompletion: { [unowned self]completion in
                        switch completion {
                        case .finished:
                            showPass = true
                        case .failure(let error):
                            print("Error: \(error)")
                        }
                        
                        isLoading = false
                        loadPassSubscription = nil
                    },
                    receiveValue: { [unowned self]result in
                        pass = result
                    }
                )
        }
    }
}

struct ModifyCertificate: View {
    
    @Environment(\.presentationMode) var presentation
    @StateObject var passLoader = PassLoader()
    
    @State var certificate: Certificate
    @State var certifcatePhoto: UIImage
    @State var selectedStatus: Int
    @State var validuntil = Date()
    
    let types:[Int8] = CertificateType.allCases.map{ $0.rawValue }
    let status:[Int8] = CertificateStatus.allCases.map{ $0.rawValue }
    let onChange: (ModifyCertificateResponse) -> Void
    
    var body: some View {
        let statusColor = Color("backgroundcertificatestatus_\(selectedStatus)")
        
        NavigationView {
            List {
                Section() {
                    HStack {
                        Image(uiImage:certifcatePhoto)
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(height: 120, alignment: .top)
                            .clipShape(Circle())
                            .overlay(Circle().stroke(statusColor, lineWidth: 4))
                        Spacer()
                        VStack(alignment: .trailing) {
                            Text("\(certificate.firstname!) \(certificate.lastname!)").font(.largeTitle)
                            Spacer()
                            Text("certificatetype_\(certificate.type)".localized()).font(.callout).bold()
                            
                            if( certificate.validuntil != nil) {
                                HStack {
                                    Text("addcert_validto").foregroundColor(Color(.gray)).font(.caption)
                                    Text(DateFormatter.certificate.string(from: certificate.validuntil!)).font(.caption).bold()
                                }}
                        }
                    }
                }
                Section()  {
                    VStack(alignment: .leading) {
                        Text("addcert_status").foregroundColor(Color(.gray)).font(.caption)
                        Picker(selection: $selectedStatus, label: Text("Certification Status")) {
                            ForEach(status.indices) { i in
                                Text("certificatestatus_\(status[i])".localized()).tag(i)
                            }
                        }.pickerStyle(SegmentedPickerStyle())
                    }
                }
                Section() {
                    VStack(alignment: .leading) {
                        Text("addcert_phonenumber").foregroundColor(Color(.gray)).font(.caption)
                        Text(certificate.phonenumber!)
                    }
                    VStack(alignment: .leading) {
                        Text("addcert_email").foregroundColor(Color(.gray)).font(.caption)
                        Text(certificate.email!)
                    }
                }
                Section {
                    DatePicker(selection: $validuntil, displayedComponents: [.hourAndMinute, .date]) {
                        Text("addcert_validto").foregroundColor(Color(.gray))
                    }
                }
                Section {
                    Button(action: modifyCertificateAction) {
                        Text("modifycert_updatebutton")
                    }
                }
                
                if( certificate.passid != nil ) {
                    Section {
                        Button(action: { passLoader.loadingPass(passid: certificate.passid!)} ) {
                            Text("modifycert_showpass")
                        }
                        .disabled(passLoader.isLoading)
                    }
                }
            }
            .listStyle(InsetGroupedListStyle())
            .sheet(isPresented: $passLoader.showPass ) {
                PassView(data: passLoader.pass!)
            }
        }
    }
    
    private func modifyCertificateAction() {
        self.presentation.wrappedValue.dismiss()
        
        certificate.validuntil = validuntil
        
        onChange(
            ModifyCertificateResponse(
                cert:certificate,
                selectedStatus:CertificateStatus(rawValue: Int8(selectedStatus))!,
                shouldDelete:false
            )
        )
    }
}


struct ModifyCertificate_Previews: PreviewProvider {
    
    static var previews: some View {
        let context = (UIApplication.shared.delegate as!AppDelegate).persistentContainer.viewContext
        let photo = context.createPicture(image: UIImage(named: "passimg")!)
        
        let certificate = context.createCertificate(
            firstName:"Hugo",
            lastName:"Meier",
            phoneNumber:"08945566",
            email:"bla@fasel.de",
            status: CertificateStatus.unknown,
            type: CertificateType.rapidtest,
            pictureid: photo.id!
        )
        
        ModifyCertificate(
            certificate: certificate,
            certifcatePhoto: UIImage(named: "passimg")!,
            selectedStatus: Int(certificate.status)
        ) { _ in }
    }
}
