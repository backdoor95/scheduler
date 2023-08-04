package com.fastcampus.minischeduler.core.auth.session;

import com.fastcampus.minischeduler.core.exception.Exception401;
import com.fastcampus.minischeduler.core.exception.Exception500;
import com.fastcampus.minischeduler.core.utils.AES256Utils;
import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AES256Utils aes256Utils;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        try {
            email = aes256Utils.encryptAES256(email);
        } catch (Exception e) {
            throw new Exception500("디코딩에 실패하였습니다");
        }

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new Exception401("인증되지 않았습니다")
        );

        return new MyUserDetails(user);
    }
}
