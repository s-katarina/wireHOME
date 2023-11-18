package projectnwt2023.backend.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import projectnwt2023.backend.helper.Constants;

import java.io.IOException;

@Service
public class MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    public String sendTextEmail(String emailTo, String subject, String textContent) throws IOException {
        Email from = new Email(Constants.fromEmail);
        Email to = new Email(emailTo);
        Content content = new Content("text/plain", textContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(Constants.sendgridKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            logger.info(response.getBody());
            return response.getBody();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }



    public String sendTextEmailMilos(String toEmail, String sub, String text) throws IOException {
        Email from = new Email(Constants.fromEmail);
        String subject = sub;
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", text);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(Constants.sendgridKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            logger.info(response.getBody());
            return response.getBody();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
}