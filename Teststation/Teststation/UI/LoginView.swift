import SwiftUI
import CoreData

struct LoginView: View {
    @EnvironmentObject var teststationState: TeststationState
    @State var username: String = ""
    @State var password: String = ""
    
    var body: some View {
        VStack {
            Spacer()
            Text("loginview_title").font(.largeTitle)
            Spacer(minLength: 20)
            Image("passdefaultimg")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 180, height: 180, alignment: /*@START_MENU_TOKEN@*/.center/*@END_MENU_TOKEN@*/)
                .clipShape(Circle())
                .overlay(
                    Circle().stroke(Color.gray, lineWidth: 4)
                )
                .shadow(radius: 4)
            Spacer(minLength: 10)
            VStack(alignment: .leading) {
                Text("loginview_username").font(.caption)
                TextField("loginview_username_placeholder", text: $username)
            }
            VStack(alignment:.leading){
                Text("loginview_password").font(.caption)
                TextField("loginview_password_placeholder", text: $password)
            }
            Spacer(minLength: 10)
            Button( action: login) {
                Text("loginview_loginbutton")
            }
            Spacer(minLength: 20)
        }
        .padding(EdgeInsets(top: 0, leading: 40, bottom: 0, trailing: 40))
    }
    
    private func login() {
        
        if( !username.isEmpty && password == "123" ) {
            teststationState.employeeId = username
            teststationState.teststationId = "1"
        }
    }
}

struct LoginView_Previews: PreviewProvider {
    
    static var previews: some View {
        
        Group {
            LoginView().environmentObject(TeststationState())
        }
    }
}
